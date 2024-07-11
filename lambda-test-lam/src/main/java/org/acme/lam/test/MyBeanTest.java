package org.acme.lam.test;

import org.acme.lam.beans.MyBean0;
import org.acme.lam.beans.MyBean1;
import org.acme.lam.beans.MyBean2;
import org.acme.lam.beans.MyBean3;
import org.acme.lam.beans.MyBean4;
import org.acme.lam.beans.MyBean5;
import org.acme.lam.beans.MyBean6;
import org.acme.lam.beans.MyBean7;
import org.acme.lam.beans.MyBean8;
import org.acme.lam.beans.MyBean9;

public class MyBeanTest {

    private final MyBean0 myBean0 = new MyBean0();
    private final MyBean1 myBean1 = new MyBean1();
    private final MyBean2 myBean2 = new MyBean2();
    private final MyBean3 myBean3 = new MyBean3();
    private final MyBean4 myBean4 = new MyBean4();
    private final MyBean5 myBean5 = new MyBean5();
    private final MyBean6 myBean6 = new MyBean6();
    private final MyBean7 myBean7 = new MyBean7();
    private final MyBean8 myBean8 = new MyBean8();
    private final MyBean9 myBean9 = new MyBean9();

    public int ping(int val) {
        val = new MyBean0().ping(val).get();
        val = new MyBean1().ping(val).get();
        val = new MyBean2().ping(val).get();
        val = new MyBean3().ping(val).get();
        val = new MyBean4().ping(val).get();
        val = new MyBean5().ping(val).get();
        val = new MyBean6().ping(val).get();
        val = new MyBean7().ping(val).get();
        val = new MyBean8().ping(val).get();
        val = new MyBean9().ping(val).get();
        return val;
    }

    public int pingStateful(int val) {
        val = this.myBean0.ping(val).get();
        val = this.myBean1.ping(val).get();
        val = this.myBean2.ping(val).get();
        val = this.myBean3.ping(val).get();
        val = this.myBean4.ping(val).get();
        val = this.myBean5.ping(val).get();
        val = this.myBean6.ping(val).get();
        val = this.myBean7.ping(val).get();
        val = this.myBean8.ping(val).get();
        val = this.myBean9.ping(val).get();
        return val;
    }

    public int getBeanCount() {
        return 10;
    }

}
