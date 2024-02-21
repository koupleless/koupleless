package mock;

/**
 * @author CodeNoobKing
 * @date 2024/2/21
 */
public class MockTestRunner {

    public void execute(Class<?> clz) {
        // use reflection to call the test method of the object
        try {
            clz.getMethod("test").invoke(clz.newInstance());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
