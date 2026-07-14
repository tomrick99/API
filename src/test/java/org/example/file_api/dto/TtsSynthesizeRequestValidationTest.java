package org.example.file_api.dto;


import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//只验证TtsSynthesizeRequest上的注解规则写的对不对
class TtsSynthesizeRequestValidationTest {

    //这个测试类里保存的一个效验器对象
    private final Validator validator;

    //测试类的构造器 JUnit创建这个测试类对象时会先执行这
    TtsSynthesizeRequestValidationTest() {
        //创建一个效验器工厂 可以理解成生产Validator的地方
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        //把这个效验器存到当前测试对象里 后面每一个测试方法都可以用; 从工厂里拿到正真干活的Validator
        this.validator = factory.getValidator();
    }

    //告诉JUnit:这是一个测试方法 请运行它
    @Test
    void shouldRejectBlankTest() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();//创建一个请求对象 像前端传JSON被转成Java对象一样
        request.setText("");    //故意给一个非法空字符串

        var violations = validator.validate(request);   //保存所有效验错误 text违反了NotBlank

        System.out.println("violations size = " + violations.size());   //输出断言有几个
        assertFalse(violations.isEmpty());  //断言错误列表不应该为空 因为故意传了空字符串坏数据 肯定得有错
    }

    @Test
    void shouldAcceptValidTest() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        request.setText("hello");
        request.setSpeed(50);
        request.setVolume(50);
        request.setPitch(50);

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());   //所有值都是正常值 violations违规行为就是空
    }

    @Test
    void shoudRejectInvalidSpeed() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        request.setText("hello");
        request.setSpeed(101);

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());

    }

    @Test
    void shoudRejectInvalidVolume() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        request.setText("hello");
        request.setVolume(101);

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shoudRejectInvalidPitch() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        request.setText("hello");
        request.setPitch(-1);

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

}
