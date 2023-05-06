public class Main {
    public static void main(String[] args) {
        //viene usato questo set property per togliere lo scaling della UI di Windows
        System.setProperty("sun.java2d.uiScale", "1");
        MainFrame frame = new MainFrame();
    }
}
