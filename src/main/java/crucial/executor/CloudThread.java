package crucial.executor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;

public abstract class CloudThread extends Thread {
    private Runnable target;
    private boolean local = false;
    protected boolean logs = true;

    public CloudThread(Runnable target) {
        this.setName("Cloud" + this.getName());
        this.target = target;
    }

    @Override
    public void run() {
//        Class klass = target.getClass();
//        if (isInnerClass(klass) && !isStaticClass(klass)) {
//            throw new RuntimeException(this.printPrefix()
//                    + "Illegal class definition. Cannot be inner unless static.");
//        }
        if (target == null) throw new NullPointerException();
        if (!(target instanceof Serializable))
            throw new IllegalArgumentException("Tasks must be Serializable");

        System.out.println(this.printPrefix() + "Start CloudThread.");

        crucial.executor.ThreadCall call = new crucial.executor.ThreadCall(this.getName());
        call.setTarget(target);

        try {
            byte[] bytesCall = crucial.executor.ByteMarshaller.toBytes(call);
            if (local) invokeLocal(bytesCall);
            else invoke(bytesCall);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println(this.printPrefix() + "Exit CloudThread.");
        }
    }

    protected String printPrefix() {
        return "[" + this.getName() + "] ";
    }

    protected abstract void invoke(byte[] threadCall);

    private void invokeLocal(byte[] threadCall) {
        crucial.executor.CloudThreadHandler handler = new crucial.executor.CloudThreadHandler();
        handler.handle(threadCall);
    }

    /**
     * Check if the given class is an inner class. Defined inside another
     * class.
     *
     * @param k The class to check.
     * @return True if the class is inner. False otherwise.
     */
    private boolean isInnerClass(Class k) {
        return k.getEnclosingClass() != null;
    }

    /**
     * Check if the given class is static.
     *
     * @param k The class to check.
     * @return True if the class is static. False otherwise.
     */
    private boolean isStaticClass(Class k) {
        return Modifier.isStatic(k.getModifiers());
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public void setLogs(boolean logs) {
        this.logs = logs;
    }

}