package com.alibaba.fastjson3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Fastjson3MapperHolderTest {

    @AfterEach
    void resetHolder() {
        // Don't leak custom mappers into sibling tests in this JVM.
        Fastjson3MapperHolder.reset();
    }

    @Test
    void defaultsToShared() {
        assertSame(ObjectMapper.shared(), Fastjson3MapperHolder.get());
    }

    @Test
    void setReplacesDefault() {
        ObjectMapper custom = ObjectMapper.builder().build();
        Fastjson3MapperHolder.set(custom);
        assertSame(custom, Fastjson3MapperHolder.get());
        assertNotSame(ObjectMapper.shared(), Fastjson3MapperHolder.get());
    }

    @Test
    void resetRestoresShared() {
        Fastjson3MapperHolder.set(ObjectMapper.builder().build());
        Fastjson3MapperHolder.reset();
        assertSame(ObjectMapper.shared(), Fastjson3MapperHolder.get());
    }

    @Test
    void nullMapperRejected() {
        assertThrows(IllegalArgumentException.class, () -> Fastjson3MapperHolder.set(null));
    }

    @Test
    void getReturnsLatestSet() {
        ObjectMapper m1 = ObjectMapper.builder().build();
        ObjectMapper m2 = ObjectMapper.builder().build();
        Fastjson3MapperHolder.set(m1);
        assertSame(m1, Fastjson3MapperHolder.get());
        Fastjson3MapperHolder.set(m2);
        assertSame(m2, Fastjson3MapperHolder.get());
    }
}
