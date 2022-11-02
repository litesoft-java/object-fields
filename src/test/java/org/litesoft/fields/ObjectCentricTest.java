package org.litesoft.fields;

import java.util.List;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.Test;
import org.litesoft.annotations.Significant;
import org.litesoft.exceptions.ExceededMaxLengthException;
import org.litesoft.utils.TemplatedMessageException;

import static org.junit.jupiter.api.Assertions.*;

class ObjectCentricTest {

    ChildISO childISO = new ChildISO( "Pebbles", 1, " Childcare Socialization ", new Parent( "Wilma" ), new Parent( "Fred" ) );
    ChildDTO childDTO = new ChildDTO( childISO );
    SubChildDTO subChildDTO = new SubChildDTO( childDTO );

    @Test
    void test_methods_ChildISO() {
        assertEquals( String.join( "\n"
                , "name: 'Pebbles'"
                , "age: 1"
                , "educationLevel: ' Childcare Socialization '"
                , "parent1:"
                , "  name: 'Wilma'"
                , "parent2:"
                , "  name: 'Fred'"
                , "description: null" // No Newline
        ).replace( '\'', '"' ), childISO.toString() );

        expectErrors( 0 );

        assertEquals( String.join( "\n"
                , "name: 'Pebbles'"
                , "age: 1"
                , "educationLevel: 'Childcare Socialization'"
                , "parent1:"
                , "  name: 'Wilma'"
                , "parent2:"
                , "  name: 'Fred'"
                , "description: 'Pebbles @ 1 year old'" // No Newline
        ).replace( '\'', '"' ), childISO.toString() );

        childISO.setAge( 2 );
        int age = ChildISO.ISO_FAS.getValue( childISO, "age" );
        assertEquals( 2, age );
        ChildISO.ISO_FAS.setValue( childISO, "age", age + 1 );
        assertEquals( 3, childISO.getAge() );

        assertEquals( String.join( "\n"
                , "name: 'Pebbles'"
                , "age: 3"
                , "educationLevel: 'Childcare Socialization'"
                , "parent1:"
                , "  name: 'Wilma'"
                , "parent2:"
                , "  name: 'Fred'"
                , "description: 'Pebbles @ 1 year old'" // No Newline
        ).replace( '\'', '"' ), childISO.toString() );

        assertEquals( String.join( "\n",
                                   "" +
                                   "name           String(36) (required)",
                                   "age            Integer",
                                   "educationLevel String",
                                   "parent1        Parent     (gender? & more metaData)",
                                   "parent2        Parent",
                                   "description    String",
                                   "" // Newline!
        ), ChildISO.ISO_FAS.toString() );

        expectErrors( 0 );

        childISO.setName( "  " );
        expectedError( expectErrors( 1 ), "name", RequiredFieldInsignificantException.MSG, String::equals );

        childISO.setName( "1234567-101234567-201234567-301234567" );
        expectedError( expectErrors( 1 ), "name", ExceededMaxLengthException.MSG_PREFIX, String::startsWith );

        childISO.setName( "Pebbles" ); // restore the OK data

        childISO.setAge( -1 );
        expectedError( expectErrors( 1 ), "age", ChildISO.AGE_NEGATIVE_PREFIX, String::startsWith );

        childISO.setAge( 0 );
        expectedError( expectErrors( 1 ), ChildISO.TOO_YOUNG_VALIDATOR_NAME, ChildISO.TOO_YOUNG_MSG, String::equals );
    }

    @SuppressWarnings("UnusedReturnValue")
    private List<FieldError> expectedError( List<FieldError> errors, String expectedFieldName, String expectedMsgPortion, BiPredicate<String, String> tester ) {
        FieldError error = errors.get( 0 );
        assertEquals( expectedFieldName, error.getFieldName() );
        assertTrue( tester.test( error.getFmtString(), expectedMsgPortion ), expectedFieldName );
        return errors.subList( 1, errors.size() );
    }

    private List<FieldError> expectErrors( int expected ) {
        List<FieldError> errors = childISO.validate();
        assertEquals( expected, errors.size() );
        return errors;
    }

    @Test
    void test_methods_ChildDTO() {
        assertEquals( String.join( "\n"
                              , "name: 'Pebbles'"
                              , "age: 1"
                              , "parent1name: 'Wilma'"
                              , "parent2name: 'Fred'" // No Newline
                      ).replace( '\'', '"' ),
                      childDTO.toString() );

        assertEquals( String.join( "\n",
                                   "" +
                                   "name        String",
                                   "age         Integer",
                                   "parent1name String",
                                   "parent2name String",
                                   "" // Newline!
        ), ChildDTO.DTO_FAS.toString() );

        ChildDTO c2 = new ChildDTO( childDTO );

        assertEquals( childDTO, c2 );
        assertEquals( childDTO.hashCode(), c2.hashCode() );
        c2.setAge( c2.getAge() + 5 );

        assertNotEquals( childDTO, c2 );
        assertNotEquals( childDTO.hashCode(), c2.hashCode() );
    }

    @Test
    @SuppressWarnings("SimplifiableAssertion")
    void test_methods_ChildDTO_SubChildDTO_equals() {
        assertFalse( childDTO.equals( subChildDTO ) ); // Since order matters, don't delegate the equality check to the test library
        assertTrue( subChildDTO.equals( childDTO ) ); // Since order matters, don't delegate the equality check to the test library
    }

    private record Parent(String name) implements Indentable {
        @Override
        public void addFieldsTo( ToStringBuilder builder ) {
            builder.add( "name", name );
        }

        @Override
        public String toString() {
            return "Parent '" + name + "'";
        }
    }

    private static final class ChildISO {
        public static final String TOO_YOUNG_VALIDATOR_NAME = "tooYoung";
        public static final String TOO_YOUNG_MSG = "education before 1 year old is not allowed";
        public static final String AGE_NEGATIVE_PREFIX = "age may not be negative, but was: ";

        static FieldAccessors<ChildISO> ISO_FAS = FieldAccessors.of( ChildISO.class )
                .required( "name", ChildISO::getName, ChildISO::setName ).withType( String.class, Significant.ConstrainTo::valueOrNull, Significant.Check::value ).addMaxLength( 36 )
                .optional( "age", ChildISO::getAge, ChildISO::setAge ).withType( Integer.class, ChildISO::limitAge )
                .optional( "educationLevel", ChildISO::getEducationLevel, ChildISO::setEducationLevel ).withType( String.class, Significant.ConstrainTo::valueOrNull )
                .optional( "parent1", ChildISO::getParent1 ).withType( Parent.class ).withMetaData( "gender?" ).addMetaData( "more metaData" )
                .optional( "parent2", ChildISO::getParent2 ).withType( Parent.class )
                .optional( "description", ChildISO::getDescription, ChildISO::generateDescription ).withType( String.class )
                .addValidator( TOO_YOUNG_VALIDATOR_NAME, TOO_YOUNG_MSG, child -> (child.getEducationLevel() != null) && (child.getAge() == 0) )
                .addValueGenerator( "aug:description", ChildISO::augmentDescription )
                .done();

        private String name;
        private int age;
        private String educationLevel;
        private final Parent parent1;
        private final Parent parent2;

        private String description;

        private ChildISO( String name, int age, String educationLevel, Parent parent1, Parent parent2 ) {
            this.name = name;
            this.age = age;
            this.educationLevel = educationLevel;
            this.parent1 = parent1;
            this.parent2 = parent2;
        }

        public List<FieldError> validate() {
            return ISO_FAS.validate( this );
        }

        @Override
        public String toString() {
            return new ToStringBuilder().addAll( this, ISO_FAS ).toString();
        }

        public String getName() {
            return name;
        }

        public void setName( String name ) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge( int age ) {
            this.age = age;
        }

        public String getEducationLevel() {
            return educationLevel;
        }

        public void setEducationLevel( String educationLevel ) {
            this.educationLevel = educationLevel;
        }

        public String getDescription() {
            return description;
        }

        public Parent getParent1() {
            return parent1;
        }

        public Parent getParent2() {
            return parent2;
        }

        @Override
        @SuppressWarnings("com.haulmont.jpb.EqualsDoesntCheckParameterClass")
        public boolean equals( Object o ) {
            return ISO_FAS.equalInstancesWithEqualTypes( this, o );
        }

        @Override
        public int hashCode() {
            return ISO_FAS.hashCodeFrom( this );
        }

        private static void limitAge( Integer age ) {
            if ( (age != null) && (age < 0) ) {
                throw new TemplatedMessageException( AGE_NEGATIVE_PREFIX + ".|0|.", "" + age );
            }
        }

        private static void generateDescription( ChildISO iso ) {
            iso.description = iso.getName() + " @ " + iso.getAge();
        }

        private static void augmentDescription( ChildISO iso ) {
            iso.description += " " + ((iso.getAge() == 1) ? "year" : "years") + " old";
        }
    }

    private static class ChildDTO {
        static FieldAccessors<ChildDTO> DTO_FAS = FieldAccessors.of( ChildDTO.class )
                .optional( "name", ChildDTO::getName, ChildDTO::setName ).withType( String.class )
                .optional( "age", ChildDTO::getAge, ChildDTO::setAge ).withType( Integer.class )
                .optional( "parent1name", ChildDTO::getParent1name, ChildDTO::setParent1name ).withType( String.class )
                .optional( "parent2name", ChildDTO::getParent2name, ChildDTO::setParent2name ).withType( String.class )
                .done();

        static FieldMappers<ChildDTO, ChildISO> FROM_CHILD_ISO_MAPPER = FieldMappers.of( ChildDTO.class, ChildISO.class )
                .add( "name", DTO_FAS, ChildISO.ISO_FAS )
                .add( "age", DTO_FAS, ChildISO.ISO_FAS )
                .add( "parent1name", DTO_FAS, "parent1", ChildISO.ISO_FAS, ChildDTO::extractParentName )
                .add( "parent2name", DTO_FAS, "parent2", ChildISO.ISO_FAS, ChildDTO::extractParentName );

        private static String extractParentName( Parent parent ) {
            return (parent == null) ? null : parent.name();
        }

        private String name;
        private Integer age;
        private String parent1name;
        private String parent2name;

        public ChildDTO() {
        }

        public ChildDTO( ChildISO childISO ) {
            FROM_CHILD_ISO_MAPPER.map( this, childISO );
        }

        @SuppressWarnings("CopyConstructorMissesField")
        public ChildDTO( ChildDTO childDTO ) {
            DTO_FAS.populateUs( this, childDTO );
        }

        @Override
        public String toString() {
            return new ToStringBuilder().addAll( this, DTO_FAS ).toString();
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getParent1name() {
            return parent1name;
        }

        public String getParent2name() {
            return parent2name;
        }

        public void setName( String pName ) {
            name = pName;
        }

        public void setAge( Integer pAge ) {
            age = pAge;
        }

        public void setParent1name( String pParent1name ) {
            parent1name = pParent1name;
        }

        public void setParent2name( String pParent2name ) {
            parent2name = pParent2name;
        }

        @Override
        @SuppressWarnings("com.haulmont.jpb.EqualsDoesntCheckParameterClass")
        public boolean equals( Object o ) {
            return DTO_FAS.equalInstancesWithEqualTypes( this, o ); // ChildDTO.equals(SubChildDTO) -> false
        }

        @Override
        public int hashCode() {
            return DTO_FAS.hashCodeFrom( this );
        }
    }

    private static final class SubChildDTO extends ChildDTO {
        public SubChildDTO( ChildDTO child ) {
            DTO_FAS.populateUs( this, child );
        }

        @Override
        @SuppressWarnings("com.haulmont.jpb.EqualsDoesntCheckParameterClass")
        public boolean equals( Object o ) {
            return DTO_FAS.equalInstancesWithSubTypes( this, o ); // SubChildDTO.equals(ChildDTO) -> true
        }
    }
}