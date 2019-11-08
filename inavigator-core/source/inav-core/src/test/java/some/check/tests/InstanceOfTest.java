package some.check.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static junit.framework.TestCase.assertTrue;

@RunWith(BlockJUnit4ClassRunner.class)
public class InstanceOfTest {

    public interface InterfaceA {}

    public class A implements InterfaceA {}

    public class B extends A {}

    public class C extends A {}

    @Test
    public void tst() {
        A a = new A();
        B b = new B();
        C c = new C();
        assertTrue(c instanceof A);
        assertTrue(c instanceof InterfaceA);
        assertTrue(b instanceof InterfaceA);
        assertTrue(a instanceof InterfaceA);
    }

}
