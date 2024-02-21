package mock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author CodeNoobKing
 * @date 2024/2/21
 */
public class BaseBootStrap {
    public static AtomicBoolean IS_BOOTSTRAP_BASE_CALLED = new AtomicBoolean(false);

    public void bootstrapBase() {
        IS_BOOTSTRAP_BASE_CALLED.set(true);
    }
}
