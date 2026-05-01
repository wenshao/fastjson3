package com.alibaba.fastjson3.mybatis;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.TypeReference;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis {@link BaseTypeHandler} that round-trips column values through
 * fastjson3 JSON. The column should be declared as {@code VARCHAR}, {@code TEXT},
 * or a database-native JSON type that the driver handles as a string.
 *
 * <p>Subclass with the concrete attribute type — fastjson3 cannot infer
 * {@code T} at runtime from the parent class's type parameter (erasure on
 * this generic abstract base), so subclasses must pass a {@code Class<T>}
 * or {@code TypeReference<T>} to the constructor:
 * <pre>{@code
 *   @MappedTypes(Profile.class)
 *   public class ProfileJsonTypeHandler extends Fastjson3JsonTypeHandler<Profile> {
 *       public ProfileJsonTypeHandler() { super(Profile.class); }
 *   }
 * }</pre>
 *
 * <p>Or for parameterized types:
 * <pre>{@code
 *   public class TagsJsonTypeHandler extends Fastjson3JsonTypeHandler<List<String>> {
 *       public TagsJsonTypeHandler() {
 *           super(new TypeReference<List<String>>() {});
 *       }
 *   }
 * }</pre>
 *
 * <p>Constructor variants:
 * <ul>
 *   <li>{@link #Fastjson3JsonTypeHandler(Class)} — non-generic types</li>
 *   <li>{@link #Fastjson3JsonTypeHandler(TypeReference)} — generic types</li>
 *   <li>{@link #Fastjson3JsonTypeHandler(Class, ObjectMapper)} — custom mapper</li>
 *   <li>{@link #Fastjson3JsonTypeHandler(TypeReference, ObjectMapper)} — custom mapper, generic type</li>
 * </ul>
 *
 * <p><b>Empty-string handling</b>: an empty column string returns {@code null},
 * accommodating databases that canonicalize empty TEXT to {@code ""} rather
 * than {@code NULL}.
 *
 * <p><b>PostgreSQL JSON columns</b>: PostgreSQL's {@code json}/{@code jsonb}
 * columns require {@code setObject(i, json, java.sql.Types.OTHER)} to bind
 * without an explicit cast in SQL. This handler uses {@code setString} which
 * works for {@code VARCHAR}/{@code TEXT}. For native JSON columns either
 * cast in the SQL ({@code ?::jsonb}) or override
 * {@link #setNonNullParameter(PreparedStatement, int, Object, JdbcType)}.
 *
 * <p><b>Custom mapper note</b>: MyBatis instantiates the subclass via no-arg
 * constructor (typically through {@code TypeHandlerRegistry} scanning), so
 * the mapper is whatever the subclass passes to {@code super(...)}. To use a
 * configured mapper, hardcode it in the subclass — Spring Boot's
 * {@code spring.fastjson3.*} properties do not propagate to MyBatis-managed
 * type handlers.
 *
 * @param <T> the entity attribute type
 */
public abstract class Fastjson3JsonTypeHandler<T> extends BaseTypeHandler<T> {
    private final ObjectMapper mapper;
    private final Type targetType;

    protected Fastjson3JsonTypeHandler(Class<T> targetType) {
        this(targetType, ObjectMapper.shared());
    }

    protected Fastjson3JsonTypeHandler(TypeReference<T> targetType) {
        this(unwrap(targetType), ObjectMapper.shared());
    }

    protected Fastjson3JsonTypeHandler(Class<T> targetType, ObjectMapper mapper) {
        this((Type) targetType, mapper);
    }

    protected Fastjson3JsonTypeHandler(TypeReference<T> targetType, ObjectMapper mapper) {
        this(unwrap(targetType), mapper);
    }

    /**
     * Advanced escape hatch: pass a {@link Type} computed at runtime.
     * For simple cases prefer {@link #Fastjson3JsonTypeHandler(Class)}
     * or {@link #Fastjson3JsonTypeHandler(TypeReference)}.
     */
    protected Fastjson3JsonTypeHandler(Type targetType, ObjectMapper mapper) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.mapper = mapper;
        this.targetType = targetType;
    }

    private static Type unwrap(TypeReference<?> ref) {
        if (ref == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        return ref.getType();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, mapper.writeValueAsString(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private T parse(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return mapper.readValue(json, targetType);
    }
}
