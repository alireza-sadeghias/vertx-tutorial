package chapter4.simple;


import java.io.File;
import java.io.FileInputStream;

public class VanillaJavaRead {
    public static void main(String[] args) {
        File file = new File("pom.xml");
        byte[] buffer  = new byte[1024];
        try(FileInputStream in = new FileInputStream(file)){
            int count = in.read(buffer);
            while (count!=-1){
                System.out.println(new String(buffer,0,count));
                count=in.read(buffer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("\n --- DONE");
        }
    }
}
