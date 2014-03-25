/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rgbrator;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 *
 * @author Kandit
 */
public class Rgbrator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        Rgbrator me = new Rgbrator();
        File nameList = new File(args[0]);
        ArrayList<Task> tasks = null;
        if (nameList.isFile() == false) {
            myexit();
        }

        try {
            tasks = me.getTasks(nameList);
        } catch (FileNotFoundException ex) {
            myexit();
        }
        if (tasks != null && !tasks.isEmpty()) {
            me.processTasks(tasks);
        }

    }

    private static void myexit() {
        System.out.println("Неправильно указан файл заданий");
        System.exit(1);
    }

    private ArrayList<Task> getTasks(File nameList) throws FileNotFoundException {
        ArrayList<Task> tasks;
        tasks = new ArrayList<>();
        File red, green, blue;
        Scanner scan = new Scanner(nameList);
        while (scan.hasNext()) {
            try {
                red = new File(scan.next());
                green = new File(scan.next());
                blue = new File(scan.next());
                tasks.add(new Task(red, green, blue));
                System.out.println("Task added "+red.getName());
            } catch (taskCreationException | NoSuchElementException e) {
                break;
            }
        }

        return tasks;
    }

    private void processTasks(ArrayList<Task> tasks) {
        for (Task task : tasks) {
            try {
                task.process();
            } catch (writeImageException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

}

class Task {

    File redFile;
    File greenFile;
    File blueFile;
    int symbolsPerPixel = 4;

    public Task(File red, File green, File blue) throws taskCreationException {
        this.redFile = red;
        this.greenFile = green;
        this.blueFile = blue;
        if ((red.isFile() || blue.isFile() || green.isFile()) == false
                || (red.length() != green.length() || green.length() != blue.length())) {
            throw new taskCreationException();
        }

    }

    public void process() throws writeImageException {
        ArrayList<Integer> rgbs = getRGB();
        Image img = createImage1(rgbs);
        String name = redFile.getAbsolutePath().replaceFirst("[.][^.]+$", "")+".bmp";

        saveImage(img, "bmp", new File(name));

    }

    public ArrayList<Integer> getRGB() {
        ArrayList<Integer> rgbs = new ArrayList<>();
        try {

            Scanner inRed = new Scanner(redFile);
            Scanner inGreen = new Scanner(greenFile);
            Scanner inBlue = new Scanner(blueFile);

            while (inRed.hasNext()) {
                int redint = inRed.nextInt();
                byte red = (byte) (redint - 256);
                int greenint = inGreen.nextInt();
                byte green = (byte) (greenint - 256);
                int blueint = inBlue.nextInt();
                byte blue = (byte) (blueint - 256);
                int rgb = red;
                rgb = (rgb << 8) + green;
                rgb = (rgb << 8) + blue;
                rgbs.add(rgb);
            }

        } catch (FileNotFoundException ex) {

        }
        return rgbs;
    }

  
    
    
     private BufferedImage createImage1 (ArrayList<Integer> rgbs) {
        Dimension dm = getImgWidth(redFile, rgbs.size());
        
        BufferedImage image = new BufferedImage(dm.width, dm.height, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = Raster.createPackedRaster(DataBuffer.TYPE_INT, dm.width, dm.height, 3, 8, null);

        raster.setDataElements(0, 0, dm.width, dm.height, convertIntegers(rgbs));
        image.setData(raster);
        return image;
    }
    
    
    

    private Dimension getImgWidth(File rawFile, int pixval) {
        try {
            Scanner in = new Scanner(rawFile);
            String st = in.nextLine();
            int width = st.length() / symbolsPerPixel;
            return new Dimension(width, pixval / width);
        } catch (FileNotFoundException ex) {
            return new Dimension(0, 0);
        }
    }

    public static int[] convertIntegers(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next();
        }
        return ret;
    }

    public void saveImage(Image img, String fileFormat, File f) throws writeImageException {
        try {
            ImageIO.write((RenderedImage) img, fileFormat, f);
        } catch (IOException ex) {
            throw new writeImageException(f.getAbsolutePath());
        }
    }

}

class taskCreationException extends Exception {

}

class writeImageException extends Exception {

    public writeImageException(String imgname) {
        super("can't write result image " + imgname);
    }
}
