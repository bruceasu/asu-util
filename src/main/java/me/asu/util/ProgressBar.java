package me.asu.util;

/**
 * Ascii progress meter. On completion this will reset itself,
 * so it can be reused
 * <br /><br />
 * 100% ################################################## |
 *
 * @author suk
 */
public class ProgressBar {

    private StringBuilder progress;

    /**
     * initialize progress bar properties.
     */
    public ProgressBar() {
        init();
    }

    public static void main(String[] args) {
        ProgressBar bar = new ProgressBar();

        System.out.println("Process Starts Now!");

        bar.update(0, 100);
        for (int i = 0; i < 100; i++) {
            // do something!
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // update the progress bar
            bar.update(i, 100);
        }
        bar.update(100, 100);
        System.out.println("Process Completed!");
    }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done  an int representing the work done so far
     * @param total an int representing the total work
     */
    public void update(Number done, Number total) {
        char[] workchars = {'|', '/', '-', '\\'};
        String format = "\r%3d%% [%s %c%s] %d/%d";

        int percent = (done.intValue() * 100) / total.intValue();
        int extrachars = (percent / 2) - this.progress.length();
        int remain = 50 - (percent / 2);
        while (extrachars-- > 0) {
            progress.append('#');
        }

        String padding = Strings.dup('.', remain);
        System.out.printf(format, percent, progress, workchars[done.intValue() % workchars.length],
                padding, done, total);

        if (done.equals(total)) {
            System.out.println();
            init();
        }
    }

    private void init() {
        this.progress = new StringBuilder(60);
    }
}
