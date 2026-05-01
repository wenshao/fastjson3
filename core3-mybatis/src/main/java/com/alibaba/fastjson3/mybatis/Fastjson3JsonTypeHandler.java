package com.alibaba.fastjson3.mybatis;

import com.alibaba.fastjson3.ObjectMapper;
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
 * or a database-native JSON type (handled by the driver as a string).
 *
 * <p>Subclass with the concrete attribute type — MyBatis cannot read the type
 * argument directly because of erasure:
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
 *           super(new TypeReference<List<String>>() {}.getType());
 *       }
 *   }
 * }</pre>
 *
 * <p>Defaults to {@link ObjectMapper#shared()}; pass a configured mapper through
 * the protected constructor to customize features.
 *
 * @param <T> the entity attribute type
 */
public abstract class Fastjson3JsonTypeHandler<T> extends BaseTypeHandler<T> {
    private final ObjectMapper mapper;
    private final Type targetType;

    protected Fastjson3JsonTypeHandler(Class<T> targetType) {
        this(ObjectMapper.shared(), targetType);
    }

    protected Fastjson3JsonTypeHandler(Type targetType) {
        this(ObjectMapper.shared(), targetType);
    }

    protected Fastjson3JsonTypeHandler(ObjectMapper mapper, Type targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        this.mapper = mapper == null ? ObjectMapper.shared() : mapper;
        this.targetType = targetType;
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
