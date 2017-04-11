import javax.swing.*;

/**
 * Created by danushka on 11/24/16.
 */
public abstract class FoilMakerSwingWorker<T, V> extends SwingWorker<T, V> {
    protected FoilMakerSwingWorker nextWorker = null;

    public FoilMakerSwingWorker getNextWorker() {
        return nextWorker;
    }

    public void setNextWorker(FoilMakerSwingWorker nextWorker) {
        this.nextWorker = nextWorker;
    }
}
