import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author Sacerdos aka Илья Дычков
 */
public class URLDownloader {
    private String siteAddress = "https://stackoverflow.com";
    private String pathToSave = new File("").getAbsolutePath() + "\\output";
    private String nameMainPageFile;
    private boolean isToOpen = false;
    private String host;
    private final String PATTERN_RESOURCE = "<(img|link).*?(src|href)=\"(.+?)\".*?>";
    /**
     * Empty constructor sets default address, path to save file and the false parameter of opening
     * after saving.
     * See also
     * @see #toDownload()
     */
    public URLDownloader() {
    }
    /**
     * The constructor sets user site address, default path to save file and the false parameter of opening
     * after saving.
     * @param siteAddress user site address
     * See also
     * @see #toDownload()
     */
    public URLDownloader(String siteAddress) {
        this.siteAddress = siteAddress;
    }
    /**
     * The constructor sets user site address and path to save file. The false parameter of opening
     * after saving still stays.
     * @param siteAddress user site address
     * @param pathToSave
     * See also
     * @see #toDownload()
     */
    public URLDownloader(String siteAddress, String pathToSave) {
        this.siteAddress = siteAddress;
        this.pathToSave = pathToSave;
    }
    /**
     * The constructor sets user site address, path to save file and parameter of opening
     * after saving.
     * @param siteAddress user site address
     * @param pathToSave
     * @param isToOpen
     * See also
     * @see #toDownload()
     */
    public URLDownloader(String siteAddress, String pathToSave, boolean isToOpen) {
        this.siteAddress = siteAddress;
        this.pathToSave = pathToSave;
        this.isToOpen = isToOpen;
    }
    /**
     * Main method of site downloading. The method saves the contents of resource which URL is listed.
     * Saved file may be opened.
     * <p>
     * {@link #defineName(URLConnection)}
     * {@link #setNameMainPageFile(String)}
     * {@link #parseHTML(URLConnection)}
     * {@link #readToBytes(URLConnection)}
     * {@link #writeToFile(byte[], String)}
     * {@link #showFile(File)}
     */
    public void toDownload() throws IOException {
        URL url = new URL(getSiteAddress());
        URLConnection urlConnect = url.openConnection();
        setNameMainPageFile(defineName(urlConnect));
        String finalPath = saveFileTo(getPathToSave(), getNameMainPageFile());
        setNameMainPageFile(finalPath.substring(finalPath.lastIndexOf("\\") + 1)); //update nameMainPageFile if changed
        byte[] buf;
        if (getNameMainPageFile().endsWith(".html") || getNameMainPageFile().endsWith(".htm")) {
            buf = parseHTML(urlConnect);
        } else {
            buf = readToBytes(urlConnect);
        }

        writeToFile(buf, finalPath);
        JOptionPane.showMessageDialog(null, "Programm complete");
        if (isToOpen) {
            showFile(new File(finalPath));
        }

    }
    /**
     * The method returns the site address
     *
     * @return siteAddress
     * @throws NullPointerException if siteAddress hasn't been set yet.
     */
    private String getSiteAddress() throws NullPointerException {
        if (siteAddress == null) {
            throw new NullPointerException("at getSiteAddress()");
        }
        return siteAddress;
    }
    /**
     * The method returns the path where to save file
     *
     * @return pathToSave
     * @throws NullPointerException if path hasn't been set yet.
     */
    private String getPathToSave() throws NullPointerException {
        if (pathToSave == null) {
            throw new NullPointerException("at getPathToSave()");
        }
        return pathToSave;
    }
    /**
     * The method returns the name of site (default index.html, but varies)
     *
     * @return nameMainPageFile
     * @throws NullPointerException if name hasn't been set yet.
     */
    private String getNameMainPageFile() throws NullPointerException {
        if (nameMainPageFile == null) {
            throw new NullPointerException("at getNameMainPageFile()");
        }
        return nameMainPageFile;
    }
    /**
     * The method returns the host of site, if it hasn't been set, we define it.
     *
     * @return pathToSave
     * {@link #getSiteAddress()}
     */
    private String getHost() {
        if (host == null) {
            int endOfHostIndex = getSiteAddress().indexOf("/", getSiteAddress().indexOf("://") + 6);
            if (endOfHostIndex == -1) {
                endOfHostIndex = getSiteAddress().length();
            }
            host = getSiteAddress().substring(0, endOfHostIndex);
        }
        return host;
    }

    private void setNameMainPageFile(String nameMainPageFile) throws NullPointerException {
        if (nameMainPageFile == null) {
            throw new NullPointerException("at setNameMainPageFile(String nameMainPageFile)");
        }
        this.nameMainPageFile = nameMainPageFile;
    }
    /**
     * The method establish name of file from given url: if the URL contains only main domain, so it's
     * "index.html" as default. In other cases tne name is the last part of the
     * URL, before character '?'
     * If it's necessary, add extension
     * @throws NullPointerException if given URLConnection == null
     * @param urlCon the URLConnection object
     * @return name of file from given URL
     */
    private String defineName(URLConnection urlCon) throws NullPointerException {
        if (urlCon == null) {
            throw new NullPointerException("at defineName(URLConnection urlCon)");
        }
        String nameFile = "index.html";
        String urlStr = urlCon.toString();
        urlStr = urlStr.split("\\?")[0];
        int lastSlashIndex = urlStr.lastIndexOf("/", urlStr.length() - 2);
        if (lastSlashIndex > urlStr.indexOf("://") + 2) {
            int end = urlStr.length();
            nameFile = urlStr.substring(lastSlashIndex + 1, end);
            if (nameFile.contains("/")) {
                nameFile = nameFile.replaceAll("/", "");
            }
            String extens = urlCon.getContentType();
            System.out.println(nameFile + " " + urlCon.getContentType() + " " + extens);
            if (extens != null) {
                int beginExt = (extens.contains("+") ? extens.indexOf("+") + 1 : extens.indexOf("/") + 1);
                int endExt = (extens.contains(";") ? extens.indexOf(";") : extens.length());
                extens = extens.substring(beginExt, endExt);
            } else {
                extens = "html";
            }
            if (!nameFile.endsWith("." + extens)) {
                nameFile += ("." + extens);
            }
        }
        return nameFile;
    }
    /**
     * The method tries to save file on given path. If path hasn't existed yet, it will be created.
     * If path exists and there is a file with such name, it will ask to replace
     * by new file or not. If choice is not to replace, then it offers to
     * establish new name and this name will be checked.
     * @param path path to the file
     * @param nameFile Name of saving file
     * @return approved full path to this file
     * @throws IllegalArgumentException if one of the arguments is incorrect
     */
    private String saveFileTo(String path, String nameFile) {
        Path pathToFolder = Paths.get(path);
        File fileFolder = pathToFolder.toFile();
        if (path.length() > 1 && !fileFolder.exists()) {
            System.out.println("Path contains non-existed folder(s), so this path will be created.");
            JOptionPane.showMessageDialog(null, "Path contains non-existed folder(s), so this path will be created.");
            try {
                Files.createDirectories(pathToFolder);
            } catch (IOException ex) {
                System.out.println("Cannot create new folders!");
                JOptionPane.showMessageDialog(null, "IllegalStateException. Cannot create new folders!");
                throw new IllegalStateException();
            }
        } else {
            Path pathFile = Paths.get(path, nameFile);
            File file = pathFile.toFile();
            System.out.println("Trying to safe file as " + pathFile.toString());
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                int ans;
                while (true) {
                    ans = JOptionPane.showConfirmDialog(null, "Warning! File with this name is already exists here. Replace it by new?");
                    System.out.println("Warning! File with this name is already exists here. Replace it by new? (y|n)");
                    if (ans == 0) {
                        break;
                    } else {
                        String newNameFile = JOptionPane.showInputDialog("New name?", nameFile.substring(0, nameFile.lastIndexOf(".")) + "2");


                        newNameFile += nameFile.substring(nameFile.lastIndexOf("."), nameFile.length());
                        nameFile = newNameFile;
                        return saveFileTo(path, nameFile);
                    }
                }
            }
        }
        if (path.length() > 1 && !path.endsWith("\\") && !path.endsWith("/")) {
            path += "\\";
        }

        return path + nameFile;
    }
    /**
     * The method reads and saves elements of the HTML document from given URL
     * The HTML document are read to the String, using the necessary
     * charset(default - "utf-8"). All files are saved to the folder with name
     * "{@link #nameMainPageFile}_files" If an exception occurs in the process of
     * reading from the InputStream, the method continue to work with next URLs
     * from the HTML document. The URLs in the document will be replaced
     * by local paths and the {@link #HashSet} is needed to prevent a loading of
     * same files. Due to {@link #defineName(URLConnection)} some files may get same
     * names, so you need to decide: replace the old one or
     * give another name for new.
     *
     * @param urlCon the URLConnection object which is associated with some HTML
     * file.
     * @return bytes representing of the HTML page
     * @throws IllegalArgumentException if the argument is null or the type
     * isn't html
     * @throws IllegalStateException if any error while reading file
     */
    private byte[] parseHTML(URLConnection urlCon) {
        String contentType = urlCon.getContentType();
        if (!contentType.contains("html")) {
            JOptionPane.showMessageDialog(null, "It isn't HTML!");
            System.out.println("It isn't HTML!");
            throw new IllegalArgumentException();
        }
        String charset = contentType.substring(contentType.indexOf("=") + 1);
        if (charset == null || !contentType.contains("charset")) {
            charset = "utf-8";
        }
        String str = new String();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream(), charset))) {
            String tmp;
            while ((tmp = br.readLine()) != null) {
                str += tmp;
            }
            tmp = null;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "IOException while reading HTML!");
            System.out.println("IOException while reading HTML!");
            throw new IllegalStateException(ex);
        }
        String nameOfFolder = getNameMainPageFile().substring(0, getNameMainPageFile().lastIndexOf(".")) + "_files/";
        String pathToFilesFolder = "";
        try {
            System.out.println(getPathToSave());
            Path pathFld = Paths.get(getPathToSave(), nameOfFolder);
            File fileOfFld = pathFld.toFile();
            if (!fileOfFld.exists()) {
                pathToFilesFolder = Files.createDirectory(pathFld).toString();
            } else {
                pathToFilesFolder = pathFld.toString();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error when creating folder!");
            System.out.println("Error when creating folder!");
            throw new IllegalStateException(ex);
        }
        HashSet<String> urlSet = new HashSet<>();
        Pattern rsrcPat = Pattern.compile(PATTERN_RESOURCE);
        Matcher matcher = rsrcPat.matcher(str);
        while (matcher.find()) {
            try {
                String urlRes = matcher.group(3);
                if (urlSet.contains(urlRes)) {
                    continue;
                }
                URLConnection urlConRes;
                String origUrlRes = urlRes;
                if (urlRes.startsWith("//")) {
                    urlRes = ("http:" + urlRes);
                } else if (urlRes.startsWith("/")) {
                    urlRes = host + urlRes;
                }
                if (urlRes.contains("tp://") || urlRes.contains("tps://")) {
                    try {
                        urlConRes = new URL(urlRes).openConnection();
                    } catch (MalformedURLException ex) {
                        JOptionPane.showMessageDialog(null, "MalformedURLException!");
                        System.out.println("MalformedURLException!");
                        continue;
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "IOException while reading resource!");
                        System.out.println("IOException while reading resource!");
                        continue;
                    }

                    byte[] buf;

                    try {
                        buf = readToBytes(urlConRes);
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        JOptionPane.showMessageDialog(null, "Some exception occurs while writing to the file");
                        System.out.println("Some exception occurs while writing to the file.");
                        continue;
                    }

                    String nameOfRes = defineName(urlConRes);

                    String fullPathRes = saveFileTo(pathToFilesFolder, nameOfRes);
                    nameOfRes = fullPathRes.substring(pathToFilesFolder.length() + 1);
                    try {
                        writeToFile(buf, fullPathRes);
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(null, "Error while writing the file");
                        System.out.println("Error while writing the file");
                        continue;
                    }
                    str = str.replace(origUrlRes, nameOfFolder + nameOfRes);
                    urlSet.add(origUrlRes);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error of some resource");
                System.out.println("Error of some resource");
            }
        }
        return str.getBytes();
    }
    /**
     * The method reads from InputStream of given URL to the byte array
     *
     * @param urlCon representation of URL from which is read the resource
     * @return byte array which contains the resource from URL
     * @throws IllegalArgumentException if the argument is null
     * @throws IllegalStateException if something get wrong
     */
    private byte[] readToBytes(URLConnection urlCon)
            throws IllegalArgumentException, IllegalStateException {
        if (urlCon == null) {
            throw new IllegalArgumentException();
        }

        byte[] buf;
        int sizeContent = urlCon.getContentLength();

        if (sizeContent != -1) {
            buf = new byte[sizeContent];
        } else {
            buf = new byte[300000000];
        }

        try (InputStream isr = urlCon.getInputStream()) {
            byte[] temp = new byte[2048];
            int n;
            int begin = 0;

            while ((n = isr.read(temp)) != -1) {
                System.arraycopy(temp, 0, buf, begin, n);
                begin += n;
            }

            if (sizeContent == -1) {
                temp = new byte[begin];
                System.arraycopy(buf, 0, temp, 0, begin);
                buf = temp;
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Exception while reading from InputStream!");
            System.out.println("Exception while reading from InputStream! \n" + ex);
            throw new IllegalStateException(ex);
        }
        return buf;
    }
    /**
     * The method write the byte array to the file with given path.
     *
     * @param buf the byte array which contains something what we need to save
     * @param fullPath the full path, including name of saving file
     * @throws IllegalArgumentException if one of the arguments is null
     * @throws IllegalStateException if the error occurs in process of writing
     */
    private void writeToFile(byte[] buf, String fullPath)
            throws IllegalArgumentException, IllegalStateException {
        if (buf == null || fullPath == null) {
            throw new IllegalArgumentException();
        }

        try (FileOutputStream fos = new FileOutputStream(fullPath)) {
            fos.write(buf);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Exception while writing to file:" + e);
            System.out.println("Exception while writing to file:" + e);
            throw new IllegalStateException();
        }
    }
    /**
     * The method opens the file
     * @param file the file which is needed to open
     * @throws IllegalArgumentException if the argument is null
     * @throws IllegalStateException if something goes wrong
     */
    private void showFile(File file) throws IllegalArgumentException, IllegalStateException {
        if (file == null) {
            JOptionPane.showMessageDialog(null, "showFile: Argument is null!");
            System.out.println("showFile: Argument is null!");
            throw new IllegalArgumentException();
        } else {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error while opening file!");
                System.out.println("Error while opening file!");
                throw new IllegalStateException();
            }
        }
    }
}
