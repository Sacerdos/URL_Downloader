import javax.swing.*;
import java.io.IOException;
/**
 * @author Sacerdos aka Илья Дычков
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length==0){
            System.out.println("0");
            URLDownloader test = new URLDownloader();
            test.toDownload();
        } else if (args.length==1){
            System.out.println("1");
            URLDownloader test = new URLDownloader(args[0]);
            test.toDownload();

        } else if (args.length==2){
            System.out.println("2");
            URLDownloader test = new URLDownloader(args[0], args[1]);
            test.toDownload();

        } else if(args.length==3){
            System.out.println("3");
            URLDownloader test = new URLDownloader(args[0], args[1], Boolean.parseBoolean(args[2]));
            test.toDownload();
        } else {
            JOptionPane.showMessageDialog(null, "Incorrect arguments!");
        }
    }
}
