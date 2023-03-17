import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

public class WindowEventManager implements WindowListener {
    MainFrame context;

    public WindowEventManager(MainFrame context) {
        this.context = context;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if(this.context.uploadProgressBar.size() == 0 && this.context.downloadProgressBar.size() == 0) {
            try {
                DownloadPanel.sendForgetMeMessage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.exit(0);
        }

        for(int i = 0; i < this.context.uploadProgressBar.size(); i++) {
            FileProgressBarPanel curr = this.context.uploadProgressBar.get(i);
            if(curr.getProgressBar().getValue() == 100 || curr.getIsFailed()) continue;
            this.context.setExtendedState(JFrame.ICONIFIED);
            return;
        }
        for(int i = 0; i < this.context.downloadProgressBar.size(); i++) {
            FileProgressBarPanel curr = this.context.downloadProgressBar.get(i);
            if(curr.getProgressBar().getValue() == 100 || curr.getIsFailed()) continue;
            this.context.setExtendedState(JFrame.ICONIFIED);
            return;
        }

        try {
            DownloadPanel.sendForgetMeMessage();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        System.exit(0);

    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
