package eren.esmahan.peopledb.repository;

import eren.esmahan.peopledb.annotation.SQL;
import eren.esmahan.peopledb.model.Address;
import eren.esmahan.peopledb.model.CrudOperation;
import eren.esmahan.peopledb.model.People;
import eren.esmahan.peopledb.model.Region;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PeopleRepository  extends  CRUDRepository <People>{

    private AddressRepository addressRepository= null;

    public static final String SAVE_PERSON_SQL = """
    INSERT INTO PEOPLE
    (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS, BUSINESS_ADDRESS) 
    VALUES (?, ?, ?, ?, ?, ?, ? )""";

    public static final String FIND_BY_ID_SQL = """
            SELECT\s
            PERSON.ID,PERSON.FIRST_NAME,PERSON.LAST_NAME,PERSON.DOB,PERSON.SALARY,PERSON.HOME_ADDRESS 
            
            HOME.ID AS HOME_ID, HOME.STREET_ADDRESS AS HOME_STREET_ADDRESS ,HOME.ADDRESS2 AS HOME_ADDRESS2, HOME.CITY AS HOME_CITY,HOME.STATE AS HOME_STATE, HOME.POSTCODE AS HOME_POSTCODE, HOME.COUNTY AS HOME_COUNTY, HOME.REGION AS HOME_REGION, HOME.COUNTRY AS HOME_COUNTRY,
            BUSINESS.ID AS BUSINESS_ID, BUSINESS.STREET_ADDRESS AS BUSINESS_STREET_ADDRESS ,BUSINESS.ADDRESS2 AS BUSINESS_ADDRESS2, BUSINESS.CITY AS BUSINESS_CITY,BUSINESS.STATE AS BUSINESS_STATE, BUSINESS.POSTCODE AS BUSINESS_POSTCODE, BUSINESS.COUNTY AS BUSINESS_COUNTY, BUSINESS.REGION AS BUSINESS_REGION, BUSINESS.COUNTRY AS BUSINESS_COUNTRY,
            FROM PEOPLE AS PERSON\s
            LEFT OUTER JOIN ADDRESSES AS HOME ON PERSON.HOME_ADDRESS = HOME.ID
            LEFT OUTER JOIN ADDRESSES AS BUSINESS ON PERSON.BUSINESS_ADDRESS = BUSINESS.ID
          
            WHERE PERSON.ID=?""";
    public static final String FIND_ALL_SQL = "SELECT ID,FIRST_NAME,LAST_NAME,DOB,SALARY FROM PEOPLE";
    public static final String SELECT_COUNT_SQL = "SELECT COUNT(*) FROM PEOPLE";
    public static final String DELETE_BY_ID_SQL = "DELETE FROM PEOPLE WHERE ID=?";
    public static final String UPDATE_BY_ID_SQL = "UPDATE PEOPLE SET FIRST_NAME=?,LAST_NAME=?,DOB=?,SALARY=? WHERE ID=?";

    public PeopleRepository(Connection connection) {
        super(connection);
        addressRepository= new AddressRepository(connection);

    }


    @Override
    @SQL(value = SAVE_PERSON_SQL,operationType = CrudOperation.SAVE) // value=SAVE_PERSON_SQL
    void mapForSave(People entity, PreparedStatement statement) throws SQLException
    {


        People savedSpouse= null;
        statement.setString(1, entity.getFirstName());
        statement.setString(2, entity.getLastName());
        statement.setTimestamp(3, convertDobToTimestamp(entity.getDob()));
        statement.setBigDecimal(4,entity.getSalary());
        statement.setString(5, entity.getEmail());
        associateAddressWithPeople(statement, entity.getHomeAddress(), 6);
        associateAddressWithPeople(statement, entity.getBusinessAddress(), 7);

        if (entity.getSpouse().isPresent()){
            savedSpouse=save(entity.getSpouse().get());
            statement.setLong(8, savedSpouse.getId());
        }
        else {
            statement.setObject(8,null);
        }

    }
    private void associateAddressWithPeople(PreparedStatement statement, Optional<Address> address, int parameterIndex) throws SQLException {
        Address savedHomeAddress;
        if (address.isPresent()) {
            savedHomeAddress = addressRepository.save(address.get());
            statement.setLong(parameterIndex, savedHomeAddress.id());
        }
        else {
            statement.setObject(parameterIndex, null);
        }
    }


    @Override
    @SQL(value = UPDATE_BY_ID_SQL,operationType = CrudOperation.UPDATE)
    protected void mapForUpdate(People people, PreparedStatement ps) throws SQLException {
        ps.setString(1, people.getFirstName());
        ps.setString(2, people.getLastName());
        ps.setTimestamp(3, convertDobToTimestamp(people.getDob()));
        ps.setBigDecimal(4, people.getSalary());

    }

// We are not going to wrap this two annotation ,java do this for us something like this
//  MultiSQL(
//    @SQL( value = FIND_BY_ID_SQL,operationType = CrudOperation.FIND_BY_ID)
//    @SQL(value = FIND_ALL_SQL,operationType = CrudOperation.FIND_ALL)
//   )
 @Override
    @SQL( value = FIND_BY_ID_SQL,operationType = CrudOperation.FIND_BY_ID)
    @SQL(value = FIND_ALL_SQL,operationType = CrudOperation.FIND_ALL)
    @SQL(value = SELECT_COUNT_SQL,operationType = CrudOperation.COUNT)
    @SQL(value =DELETE_BY_ID_SQL,operationType = CrudOperation.DELETE)
    People extractEntityFromResultSet(ResultSet rs)  throws SQLException{
        long peopleId = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");


        Address homeAddress = extractAddress(rs, "HOME_");
        Address businessAddress = extractAddress(rs, "BUSINESS_");
        People spouse = extractSpouse(rs,"SPOUSE_");


        People person = new People(peopleId, firstName, lastName, dob, salary);
        person.setHomeAddress(homeAddress);
        person.setBusinessAddress(businessAddress);
        person.setSpouse(spouse);
        return person;
    }

    private People extractSpouse(ResultSet rs, String aliasPrefix) throws SQLException {
        Long spouseId = getValueByAlias(aliasPrefix + "ID", rs, Long.class);
        if(spouseId == null) {
            return null;
        }
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        Address homeAddress = extractAddress(rs, "HOME_");
        Address businessAddress = extractAddress(rs, "BUSINESS_");
        People spouse = new People(spouseId,firstName,lastName,dob,salary);
        spouse.setHomeAddress(homeAddress);
        spouse.setBusinessAddress(businessAddress);
        return spouse;

    }

    private Address extractAddress(ResultSet rs, String aliasPrefix) throws SQLException {
        Long addressId = getValueByAlias(aliasPrefix + "ID", rs, Long.class);
        if(addressId == null) {
            return null;
        }
        String streetAddress = getValueByAlias(aliasPrefix +"STREET_ADDRESS",rs,String.class);
        String address2 = getValueByAlias(aliasPrefix + "ADDRESS2",rs,String.class);
        String city = getValueByAlias(aliasPrefix + "CITY",rs,String.class);
        String state = getValueByAlias(aliasPrefix + "STATE",rs,String.class);
        String postcode = getValueByAlias(aliasPrefix + "POSTCODE",rs,String.class);
        String county = getValueByAlias(aliasPrefix + "COUNTY",rs,String.class);
        Region region = Region.valueOf(getValueByAlias(aliasPrefix + "REGION",rs,String.class).toUpperCase());
        String country = getValueByAlias(aliasPrefix + "COUNTRY",rs,String.class);
        Address address = new Address(addressId, streetAddress, address2, city, state, postcode, country, county, region);
        return address;
    }

    private <T> T getValueByAlias(String alias, ResultSet rs, Class<T> clazz) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        for (int columnIdx=1; columnIdx<=columnCount; columnIdx++ )
        {
           if(alias.equals( rs.getMetaData().getColumnLabel(columnIdx)))
           {
               return (T) rs.getObject(columnIdx);
           }
        }
        throw  new SQLException(String.format("Column not found for alias: '%s'", alias));
    }



//    @Override
//    String getFindByIdSql() {
//        return FIND_BY_ID_SQL;
//    }
//
//
//    @Override
//    protected String getFindAllSql() {
//        return FIND_ALL_SQL;
//    }
//
//    @Override
//    protected String getCountSql() {
//        return SELECT_COUNT_SQL;
//    }
//
//
//    @Override
//    protected String getDeleteSql() {
//        return DELETE_BY_ID_SQL;
//    }
//

//    @Override
//    protected String getUpdateSql() {
//        return UPDATE_BY_ID_SQL;
//    }


    private static Timestamp convertDobToTimestamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }

}
