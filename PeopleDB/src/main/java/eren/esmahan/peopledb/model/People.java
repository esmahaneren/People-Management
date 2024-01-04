package eren.esmahan.peopledb.model;

import eren.esmahan.peopledb.annotation.Id;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public class People {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private ZonedDateTime dob;
    private BigDecimal salary = new BigDecimal(0);
    private String email;
    private Optional<Address> homeAddress = Optional.empty();
    private Optional<Address> businessAddress = Optional.empty();




    public People(@Id long id, String firstName, String lastName, ZonedDateTime dob, BigDecimal salary) {
        this(id,firstName,lastName,dob);
        this.salary = salary;
    }
    public People(@Id Long id, String firstName, String lastName, ZonedDateTime dob) {
        this(firstName,lastName,dob);
        this.id = id;
    }
    public People(String firstName, String lastName, ZonedDateTime dob)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id=id;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public ZonedDateTime getDob() {
        return dob;
    }

    public void SetId(long id) {
        this.id=id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHomeAddress(Address homeAddress) {this.homeAddress=Optional.ofNullable(homeAddress);}

    public Optional<Address> getHomeAddress() {return homeAddress;}

    public void setBusinessAddress(Address businessAddress) {this.businessAddress = Optional.ofNullable(businessAddress);}

    public Optional<Address> getBusinessAddress() {return businessAddress;}



    @Override
    public String toString() {
        return "People{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob=" + dob +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        People people = (People) o;
        return Objects.equals(id, people.id) && Objects.equals(firstName, people.firstName) && Objects.equals(lastName, people.lastName) && Objects.equals(dob.withZoneSameInstant(ZoneId.of("+0")).truncatedTo(ChronoUnit.SECONDS), people.dob.withZoneSameInstant(ZoneId.of("+0")).truncatedTo(ChronoUnit.SECONDS));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, dob);
    }

}
