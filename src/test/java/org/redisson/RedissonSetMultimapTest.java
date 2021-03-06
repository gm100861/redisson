package org.redisson;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.redisson.core.RSetMultimap;

public class RedissonSetMultimapTest extends BaseTest {

    public static class SimpleKey implements Serializable {

        private String key;

        public SimpleKey() {
        }

        public SimpleKey(String field) {
            this.key = field;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "key: " + key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleKey other = (SimpleKey) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }

    }

    public static class SimpleValue implements Serializable {

        private String value;

        public SimpleValue() {
        }

        public SimpleValue(String field) {
            this.value = field;
        }

        public void setValue(String field) {
            this.value = field;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "value: " + value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleValue other = (SimpleValue) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

    }

    @Test
    public void testSize() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("0"), new SimpleValue("2"));

        assertThat(map.size()).isEqualTo(2);

        map.fastRemove(new SimpleKey("0"));

        Set<SimpleValue> s = map.get(new SimpleKey("0"));
        assertThat(s).isEmpty();
        assertThat(map.size()).isEqualTo(0);
    }
    
    @Test
    public void testKeySize() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("0"), new SimpleValue("2"));
        map.put(new SimpleKey("1"), new SimpleValue("3"));

        assertThat(map.keySize()).isEqualTo(2);
        assertThat(map.keySet().size()).isEqualTo(2);

        map.fastRemove(new SimpleKey("0"));

        Set<SimpleValue> s = map.get(new SimpleKey("0"));
        assertThat(s).isEmpty();
        assertThat(map.keySize()).isEqualTo(1);
    }

    @Test
    public void testPut() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("0"), new SimpleValue("2"));
        map.put(new SimpleKey("0"), new SimpleValue("3"));
        map.put(new SimpleKey("0"), new SimpleValue("3"));
        map.put(new SimpleKey("3"), new SimpleValue("4"));

        assertThat(map.size()).isEqualTo(4);

        Set<SimpleValue> s1 = map.get(new SimpleKey("0"));
        assertThat(s1).containsOnly(new SimpleValue("1"), new SimpleValue("2"), new SimpleValue("3"));
        Set<SimpleValue> allValues = map.getAll(new SimpleKey("0"));
        assertThat(allValues).containsOnly(new SimpleValue("1"), new SimpleValue("2"), new SimpleValue("3"));

        Set<SimpleValue> s2 = map.get(new SimpleKey("3"));
        assertThat(s2).containsOnly(new SimpleValue("4"));
    }

    @Test
    public void testRemoveAll() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("0"), new SimpleValue("2"));
        map.put(new SimpleKey("0"), new SimpleValue("3"));

        Set<SimpleValue> values = map.removeAll(new SimpleKey("0"));
        assertThat(values).containsOnly(new SimpleValue("1"), new SimpleValue("2"), new SimpleValue("3"));
        assertThat(map.size()).isZero();

        Set<SimpleValue> values2 = map.removeAll(new SimpleKey("0"));
        assertThat(values2).isEmpty();
    }

    @Test
    public void testFastRemove() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        assertThat(map.put(new SimpleKey("0"), new SimpleValue("1"))).isTrue();
        assertThat(map.put(new SimpleKey("0"), new SimpleValue("2"))).isTrue();
        assertThat(map.put(new SimpleKey("0"), new SimpleValue("2"))).isFalse();
        assertThat(map.put(new SimpleKey("0"), new SimpleValue("3"))).isTrue();

        long removed = map.fastRemove(new SimpleKey("0"), new SimpleKey("1"));
        assertThat(removed).isEqualTo(1);
        assertThat(map.size()).isZero();
    }

    @Test
    public void testContainsKey() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        assertThat(map.containsKey(new SimpleKey("0"))).isTrue();
        assertThat(map.containsKey(new SimpleKey("1"))).isFalse();
    }

    @Test
    public void testContainsValue() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));

        assertThat(map.containsValue(new SimpleValue("1"))).isTrue();
        assertThat(map.containsValue(new SimpleValue("0"))).isFalse();
    }

    @Test
    public void testContainsEntry() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));

        assertThat(map.containsEntry(new SimpleKey("0"), new SimpleValue("1"))).isTrue();
        assertThat(map.containsEntry(new SimpleKey("0"), new SimpleValue("2"))).isFalse();
    }

    @Test
    public void testRemove() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("0"), new SimpleValue("2"));
        map.put(new SimpleKey("0"), new SimpleValue("3"));

        assertThat(map.remove(new SimpleKey("0"), new SimpleValue("2"))).isTrue();
        assertThat(map.remove(new SimpleKey("0"), new SimpleValue("5"))).isFalse();
        assertThat(map.get(new SimpleKey("0")).size()).isEqualTo(2);
        assertThat(map.getAll(new SimpleKey("0")).size()).isEqualTo(2);
    }

    @Test
    public void testPutAll() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        List<SimpleValue> values = Arrays.asList(new SimpleValue("1"), new SimpleValue("2"), new SimpleValue("3"));
        assertThat(map.putAll(new SimpleKey("0"), values)).isTrue();
        assertThat(map.putAll(new SimpleKey("0"), Arrays.asList(new SimpleValue("1")))).isFalse();

        assertThat(map.get(new SimpleKey("0"))).containsOnlyElementsOf(values);
    }

    @Test
    public void testKeySet() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("3"), new SimpleValue("4"));

        assertThat(map.keySet()).containsOnly(new SimpleKey("0"), new SimpleKey("3"));
        assertThat(map.keySet().size()).isEqualTo(2);
    }

    @Test
    public void testValues() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("3"), new SimpleValue("4"));

        assertThat(map.values()).containsOnly(new SimpleValue("1"), new SimpleValue("4"));
    }

    @Test
    public void testEntrySet() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("3"), new SimpleValue("4"));

        assertThat(map.entries().size()).isEqualTo(2);
        Map<SimpleKey, SimpleValue> testMap = new HashMap<SimpleKey, SimpleValue>();
        testMap.put(new SimpleKey("0"), new SimpleValue("1"));
        testMap.put(new SimpleKey("3"), new SimpleValue("4"));
        assertThat(map.entries()).containsOnlyElementsOf(testMap.entrySet());
    }

    @Test
    public void testReplaceValues() {
        RSetMultimap<SimpleKey, SimpleValue> map = redisson.getSetMultimap("test1");
        map.put(new SimpleKey("0"), new SimpleValue("1"));
        map.put(new SimpleKey("3"), new SimpleValue("4"));

        List<SimpleValue> values = Arrays.asList(new SimpleValue("11"), new SimpleValue("12"));
        Set<SimpleValue> oldValues = map.replaceValues(new SimpleKey("0"), values);
        assertThat(oldValues).containsOnly(new SimpleValue("1"));

        Set<SimpleValue> allValues = map.getAll(new SimpleKey("0"));
        assertThat(allValues).containsOnlyElementsOf(values);
    }

    @Test
    public void testExpire() throws InterruptedException {
        RSetMultimap<String, String> map = redisson.getSetMultimap("simple");
        map.put("1", "2");
        map.put("2", "3");

        map.expire(100, TimeUnit.MILLISECONDS);

        Thread.sleep(500);

        assertThat(map.size()).isZero();
    }

    @Test
    public void testExpireAt() throws InterruptedException {
        RSetMultimap<String, String> map = redisson.getSetMultimap("simple");
        map.put("1", "2");
        map.put("2", "3");

        map.expireAt(System.currentTimeMillis() + 100);

        Thread.sleep(500);

        assertThat(map.size()).isZero();
    }

    @Test
    public void testClearExpire() throws InterruptedException {
        RSetMultimap<String, String> map = redisson.getSetMultimap("simple");
        map.put("1", "2");
        map.put("2", "3");

        map.expireAt(System.currentTimeMillis() + 100);

        map.clearExpire();

        Thread.sleep(500);

        assertThat(map.size()).isEqualTo(2);
    }

    @Test
    public void testDelete() {
        RSetMultimap<String, String> map = redisson.getSetMultimap("simple");
        map.put("1", "2");
        map.put("2", "3");
        assertThat(map.delete()).isTrue();
        
        RSetMultimap<String, String> map2 = redisson.getSetMultimap("simple1");
        assertThat(map2.delete()).isFalse();
    }

}
