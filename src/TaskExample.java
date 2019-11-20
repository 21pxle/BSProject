import javafx.concurrent.Task;

public class TaskExample extends Task<Integer> {

    public static void main(String[] args) throws Exception {
        int i = new TaskExample().call();
        System.out.println(i);
    }

    @Override
    protected Integer call() throws Exception {
        int i;
        for (i = 1; i <= 100; i++) {
            Thread.sleep(100);
            System.out.println("Iteration " + i);
        }
        return i;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage("Done!");
    }
}
