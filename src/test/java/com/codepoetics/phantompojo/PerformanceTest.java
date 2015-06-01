package com.codepoetics.phantompojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Ignore
public class PerformanceTest {

    interface Person extends PhantomPojo<Person.Builder> {
        interface Builder extends Supplier<Person> {
            Builder withName(String name);
            Builder withAge(int age);
        }

        static Builder builder() {
            return PhantomBuilder.building(Person.class);
        }

        @JsonCreator
        static Person create(Map<String, Object> data) {
            return PhantomPojo.wrapping(data).with(Person.class);
        }

        String getName();
        int getAge();
    }
    @Rule
    public final ContiPerfRule rule = new ContiPerfRule();

    private final List<Person> people = new ArrayList<>(1000000);
    private final Map<String, Object> personData = new HashMap<>();
    {
        personData.put("name" ,"Fred");
        personData.put("age", 42);
    }

    private final String json = "{\"name\":\"Fred\",\"age\":42}";
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @PerfTest(invocations = 1000, warmUp = 200)
    public void objectCreationWithBuilder() {
        for (int i = 0; i<1000; i++) {
            people.add(Person.builder().withName("Fred").withAge(42).get());
        }
    }

    @Test
    @PerfTest(invocations = 1000, warmUp = 200)
    public void objectCreationFromMap() {
        for (int i = 0; i<1000; i++) {
            people.add(PhantomPojo.wrapping(personData).with(Person.class));
        }
    }

    @Test
    @PerfTest(invocations = 1000, warmUp = 200)
    public void objectCreationFromJson() throws IOException {
        for (int i = 0; i<1000; i++) {
            people.add(mapper.readValue(json, Person.class));
        }
    }
}
