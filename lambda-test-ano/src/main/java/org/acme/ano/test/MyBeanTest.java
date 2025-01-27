package org.acme.ano.test;

import org.acme.ano.beans.MyBean0;
import org.acme.ano.beans.MyBean1;
import org.acme.ano.beans.MyBean2;
import org.acme.ano.beans.MyBean3;
import org.acme.ano.beans.MyBean4;
import org.acme.ano.beans.MyBean5;
import org.acme.ano.beans.MyBean6;
import org.acme.ano.beans.MyBean7;
import org.acme.ano.beans.MyBean8;
import org.acme.ano.beans.MyBean9;

public class MyBeanTest {

    private MyBean0 myBean0 = new MyBean0();
    private MyBean1 myBean1 = new MyBean1();
    private MyBean2 myBean2 = new MyBean2();
    private MyBean3 myBean3 = new MyBean3();
    private MyBean4 myBean4 = new MyBean4();
    private MyBean5 myBean5 = new MyBean5();
    private MyBean6 myBean6 = new MyBean6();
    private MyBean7 myBean7 = new MyBean7();
    private MyBean8 myBean8 = new MyBean8();
    private MyBean9 myBean9 = new MyBean9();

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
