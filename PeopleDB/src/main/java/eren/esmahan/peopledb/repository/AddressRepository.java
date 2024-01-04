package eren.esmahan.peopledb.repository;

import eren.esmahan.peopledb.annotation.SQL;
import eren.esmahan.peopledb.model.Address;
import eren.esmahan.peopledb.model.CrudOperation;
import eren.esmahan.peopledb.model.Region;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AddressRepository extends CRUDRepository<Address>{

    public static final String SAVE_ADDRESS_SQL = """
            INSERT INTO ADDRESSES (STREET_ADDRESS,ADDRESS2,CITY,STATE,POSTCODE,COUNTY,REGION,COUNTRY)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";
    public static final String FIND_BY_ID_SQL = """
            SELECT ID,STREET_ADDRESS,ADDRESS2,CITY,STATE,POSTCODE,COUNTY,REGION,COUNTRY 
            FROM ADDRESSES WHERE ID= ?
            """;

    public AddressRepository(Connection connection) {
        super(connection);
    }


    @Override
    @SQL(value = SAVE_ADDRESS_SQL,operationType = CrudOperation.SAVE)
    void mapForSave(Address entity, PreparedStatement statement) throws SQLException {
        statement.setString(1, entity.streetAddress());
        statement.setString(2, entity.address2());
        statement.setString(3, entity.city());
        statement.setString(4, entity.state());
        statement.setString(5, entity.postcode());
        statement.setString(6, entity.county());
        statement.setString(7, entity.region().toString());
        statement.setString(8, entity.country());

    }

    @Override
    @SQL(value =  FIND_BY_ID_SQL,operationType = CrudOperation.FIND_BY_ID)
    Address extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("ID");
        String streetAddress = rs.getString("STREET_ADDRESS");
        String address2 = rs.getString("ADDRESS2");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String postcode = rs.getString("POSTCODE");
        String county = rs.getString("COUNTY");
        Region region = Region.valueOf(rs.getString("REGION").toUpperCase());
        String country = rs.getString("COUNTRY");

        Address address = new Address(id, streetAddress, address2, city, state, postcode, country, county, region);
        return address;
    }


    @Override
    protected void mapForUpdate(Address entity, PreparedStatement ps) throws SQLException {

    }
}
