import javax.swing.*;

public class ActionSelect {
    private JButton button1;
    private JPanel panel1;

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        JFrame frame = new JFrame("ActionSelect");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        frame.setContentPane(new ActionSelect().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
