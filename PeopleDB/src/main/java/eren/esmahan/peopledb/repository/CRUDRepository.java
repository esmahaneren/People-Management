package eren.esmahan.peopledb.repository;

import eren.esmahan.peopledb.annotation.Id;
import eren.esmahan.peopledb.annotation.MultiSQL;
import eren.esmahan.peopledb.annotation.SQL;
import eren.esmahan.peopledb.model.CrudOperation;


import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

abstract class CRUDRepository <T >{

    protected Connection connection;

    public CRUDRepository(Connection connection) {
        this.connection = connection;
    }

    public T save(T entity)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement(getSqlByAnnotation(CrudOperation.SAVE, this::getSaveSql), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity,statement);
            int recordsAffected = statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            while(resultSet.next())
            {
                long id = resultSet.getLong(1);
                setIdByAnnotation(id, entity);
               // System.out.println(entity);
            }
          //  System.out.printf("Records affected: %d%n", recordsAffected);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }


    public Optional<T> findById(Long id) {
        T entity = null;

        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_BY_ID,this::getFindByIdSql));
            ps.setLong(1,id);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                entity=extractEntityFromResultSet(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(entity);
    }


    public List<T> findAll()
    {
        List<T> entities = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_ALL,this::getFindAllSql));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                entities.add(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return entities;
    }

    public long count()
    {
        long count = 0;

        try
        {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.COUNT,this::getCountSql));
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                count = rs.getLong(1);
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return count;
    }



    private void setIdByAnnotation(Long id, T entity)
    {
        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .forEach(f -> {
                    f.setAccessible(true);
                    try {
                        f.set(entity, id);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unable to set ID field value");
                    }
                });
    }

    private Long getIdByAnnotation(T entity)
    {
      return  Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .map(f -> {
                    f.setAccessible(true);
                    Long id= null;
                    try {
                       id= (long)f.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return id;
                })
                .findFirst().orElseThrow(()-> new RuntimeException("No ID annotation field found"));
    }
    public void delete(T entity)
    {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.DELETE,this::getDeleteSql));
            ps.setLong(1, getIdByAnnotation(entity));
            int affectedRecordCount = ps.executeUpdate();
            System.out.println(affectedRecordCount);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void delete(T ...entities) //People[] people
    {
        for (T array : entities) {
            delete(array);
        }


//  another way to the same thing with SQL
//        try  {
//            Statement st = connection.createStatement();
//            String ids = Arrays.stream(entities)
//                    .map(People::getId)
//                    .map(String::valueOf)
//                    .collect(Collectors.joining(","));
//
//            int affectedRecordCount = st.executeUpdate("DELETE FROM PEOPLE WHERE ID IN (:ids)".replace("ids", ids));
//            System.out.println(affectedRecordCount);
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }


    }
    public void update(T entity)
    {

        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.UPDATE,this::getUpdateSql));
            mapForUpdate(entity, ps);
            ps.setLong(5, getIdByAnnotation(entity));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private String getSqlByAnnotation(CrudOperation operationType, Supplier<String> sqlGetter)
    {
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(MultiSQL.class))
                .map(m -> m.getAnnotation(MultiSQL.class))
                .flatMap(msql -> Arrays.stream(msql.value()));


        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(SQL.class))
                .map(m -> m.getAnnotation(SQL.class));


        return  Stream.concat(multiSqlStream,sqlStream)
                .filter(a -> a.operationType().equals(operationType))
                .map(SQL::value)
                .findFirst().orElseGet(sqlGetter);

        // go find all the SQL stream  im looking for via this multi sql approach
        // and also find all the Sql stream with the original approach we had
        // with these two streams,concatenate those together


    }
    abstract T extractEntityFromResultSet(ResultSet rs) throws SQLException;


    abstract void mapForSave(T entity, PreparedStatement statement)  throws SQLException;

    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException;




    protected String getUpdateSql(){
        throw new RuntimeException("SQL not defined");
    };

    protected String getDeleteSql(){
        throw new RuntimeException("SQL not defined.");
    }

    protected  String getCountSql(){
        throw new RuntimeException("SQL not defined.");
    }

    protected  String getFindAllSql(){
        throw new RuntimeException("SQL not defined.");
    }

   protected String getSaveSql(){
         throw new RuntimeException("SQL not defined.");
     };
    protected String getFindByIdSql(){
        throw  new RuntimeException("SQL not defined.");
     }


}
