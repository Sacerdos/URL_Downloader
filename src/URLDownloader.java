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

public class URLDownloader {
    private String siteAdress="https://stackoverflow.com";
    private String pathToSave=new File("").getAbsolutePath() + "\\output";
    private String nameMainPageFile;
    private boolean isToOpen=false;
    private String host;
    private final String PATTERN_RESOURCE = "<(img|link).*?(src|href)=\"(.+?)\".*?>";
    public URLDownloader() {
    }
    public URLDownloader(String siteAdress) {
        this.siteAdress = siteAdress;
    }
    public URLDownloader(String siteAdress, String pathToSave) {
        this.siteAdress = siteAdress;
        this.pathToSave = pathToSave;
    }

    public URLDownloader(String siteAdress, String pathToSave, boolean isToOpen) {
        this.siteAdress = siteAdress;
        this.pathToSave = pathToSave;
        this.isToOpen = isToOpen;
    }
    public void toDownload() throws IOException {
        URL url = new URL(getSiteAdress());
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

    private String getSiteAdress() throws NullPointerException {
        if (siteAdress==null){
            throw new NullPointerException("at getSiteAdress()");
        }
        return siteAdress;
    }
    private String getPathToSave() throws NullPointerException {
        if (pathToSave==null){
            throw new NullPointerException("at getPathToSave()");
        }
        return pathToSave;
    }
    private String getNameMainPageFile() throws NullPointerException {
        if (nameMainPageFile==null){
            throw new NullPointerException("at getNameMainPageFile()");
        }
        return nameMainPageFile;
    }
    private String getHost() {
        if (host==null){
            int endOfHostIndex = getSiteAdress().indexOf("/", getSiteAdress().indexOf("://") + 6);
            if (endOfHostIndex  == -1) {
                endOfHostIndex = getSiteAdress().length();
            }
            host = getSiteAdress().substring(0, endOfHostIndex);
        }
        return host;
    }
    private void setNameMainPageFile(String nameMainPageFile) throws NullPointerException {
        if (nameMainPageFile == null) {
            throw new NullPointerException("at setNameMainPageFile(String nameMainPageFile)");
        }
        this.nameMainPageFile = nameMainPageFile;
    }
    private String defineName(URLConnection urlCon) throws NullPointerException {
        if (urlCon == null) {
            throw new NullPointerException("at defineName(URLConnection urlCon)");
        }
        String nameFile = "index.html";
        String urlStr = urlCon.toString();
        urlStr=urlStr.split("\\?")[0];
        int lastSlashIndex = urlStr.lastIndexOf("/", urlStr.length() - 2);
        if (lastSlashIndex > urlStr.indexOf("://") + 2) {
            int end = urlStr.length();
            nameFile = urlStr.substring(lastSlashIndex + 1, end);
            if (nameFile.contains("/")) {
                nameFile = nameFile.replaceAll("/", "");
            }
            String extens = urlCon.getContentType();
            System.out.println(nameFile + " " +urlCon.getContentType() + " " + extens);
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
                    /*try {
                        ans = reader.readLine();
                    } catch (IOException ex) {
                        throw new IllegalStateException();
                    }
                    if (ans.startsWith("y")) {
                        break;
                    }
                    if (ans.startsWith("n")) {
                        System.out.println("Change name for this file:");
                        try {
                            String newNameFile = reader.readLine();
                            newNameFile += nameFile.substring(nameFile.lastIndexOf("."), nameFile.length());
                            nameFile = newNameFile;
                        } catch (IOException ex) {
                            throw new IllegalStateException();
                        }
                        return saveFile(path, nameFile);
                    }*/
                    if(ans==0){
                        break;
                    } else{
                        String newNameFile = JOptionPane.showInputDialog("New name?");
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
                }
                else if (urlRes.startsWith("/")) {
                    urlRes = host + urlRes;
                }
                if (urlRes.contains("tp://") || urlRes.contains("tps://")) {
                    try {
                        urlConRes = new URL(urlRes).openConnection();
                    } catch (MalformedURLException ex) {
                        JOptionPane.showMessageDialog(null, "MalformedURLException! Let's proceed...");
                        System.out.println("MalformedURLException! Let's proceed...");
                        continue;
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "IOException while reading resource! Let's proceed...");
                        System.out.println("IOException while reading resource! Let's proceed...");
                        continue;
                    }

                    byte[] buf;

                    try {
                        buf = readToBytes(urlConRes);
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        JOptionPane.showMessageDialog(null, "Some exception occurs while writing "
                                + "to the file, let's proceed...");
                        System.out.println("Some exception occurs while writing "
                                + "to the file, let's proceed...");
                        continue;
                    }

                    String nameOfRes = defineName(urlConRes);

                    String fullPathRes = saveFileTo(pathToFilesFolder, nameOfRes);
                    nameOfRes = fullPathRes.substring(pathToFilesFolder.length() + 1);
                    try {
                        writeToFile(buf, fullPathRes);
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(null, "Error while writing the file, proceed...");
                        System.out.println("Error while writing the file, proceed...");
                        continue;
                    }
                    str = str.replace(origUrlRes, nameOfFolder + nameOfRes);
                    urlSet.add(origUrlRes);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error of some resource, proceed...");
                System.out.println("Error of some resource, proceed...");
            }
        }
        return str.getBytes();
    }

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

    @Override
    public String toString() {
        return "URLDownloader{" +
                "siteAdress='" + getSiteAdress() + '\'' +
                ", pathToSave='" + getPathToSave() + '\'' +
                ", nameMainPageFile='" + getNameMainPageFile() + '\'' +
                ", isToOpen=" + isToOpen +
                ", host='" + getHost() + '\'' +
                ", PATTERN_RESOURCE='" + PATTERN_RESOURCE + '\'' +
                '}';
    }
}
