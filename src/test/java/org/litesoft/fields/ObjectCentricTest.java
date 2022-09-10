package org.litesoft.fields;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectCentricTest {

    Child child = new Child( "Pebbles", 1, new Parent( "Wilma" ), new Parent( "Fred" ) );
    ChildDTO childDTO = new ChildDTO( child );
    SubChildDTO subChildDTO = new SubChildDTO( childDTO );

    @Test
    void test_methods_Child() {
        assertEquals( String.join( "\n"
                              , "name: 'Pebbles'"
                              , "age: 1"
                              , "parent1:"
                              , "  name: 'Wilma'"
                              , "parent2:"
                              , "  name: 'Fred'" // No Newline
                      ).replace( '\'', '"' ),
                      child.toString() );

        child.setAge( 2 );
        int age = Child.FAS.getValue( child, "age" );
        assertEquals( 2, age );
        Child.FAS.setValue( child, "age", age + 1 );
        assertEquals( 3, child.getAge() );

        assertEquals( String.join( "\n"
                              , "name: 'Pebbles'"
                              , "age: 3"
                              , "parent1:"
                              , "  name: 'Wilma'"
                              , "parent2:"
                              , "  name: 'Fred'" // No Newline
                      ).replace( '\'', '"' ),
                      child.toString() );

        assertEquals( String.join( "\n",
                                   "" +
                                   "name    String(36) (required)",
                                   "age     Integer",
                                   "parent1 Parent     (gender? & more metaData)",
                                   "parent2 Parent",
                                   "" // Newline!
        ), Child.FAS.toString() );
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
        ), ChildDTO.FAS.toString() );

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

    private static final class Child {
        static FieldAccessors<Child> FAS = FieldAccessors.of( Child.class )
                .required( "name", Child::getName ).withType( String.class ).addMaxLength( 36 )
                .optional( "age", Child::getAge, Child::setAge ).withType( Integer.class )
                .optional( "parent1", Child::getParent1 ).withType( Parent.class ).withMetaData( "gender?" ).addMetaData( "more metaData" )
                .optional( "parent2", Child::getParent2 ).withType( Parent.class )
                .done();

        private final String name;
        private int age;
        private final Parent parent1;
        private final Parent parent2;

        private Child( String name, int age, Parent parent1, Parent parent2 ) {
            this.name = name;
            this.age = age;
            this.parent1 = parent1;
            this.parent2 = parent2;
        }

        @Override
        public String toString() {
            return new ToStringBuilder().addAll( this, FAS ).toString();
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public void setAge( int age ) {
            this.age = age;
        }

        public Parent getParent1() {
            return parent1;
        }

        public Parent getParent2() {
            return parent2;
        }

        @Override
        public boolean equals( Object o ) {
            return FAS.equalInstancesWithEqualTypes( this, o );
        }

        @Override
        public int hashCode() {
            return FAS.hashCodeFrom( this );
        }
    }

    private static class ChildDTO {
        static FieldAccessors<ChildDTO> FAS = FieldAccessors.of( ChildDTO.class )
                .optional( "name", ChildDTO::getName, ChildDTO::setName ).withType( String.class )
                .optional( "age", ChildDTO::getAge, ChildDTO::setAge ).withType( Integer.class )
                .optional( "parent1name", ChildDTO::getParent1name, ChildDTO::setParent1name ).withType( String.class )
                .optional( "parent2name", ChildDTO::getParent2name, ChildDTO::setParent2name ).withType( String.class )
                .done();

        static FieldMappers<Child, ChildDTO> FROM_CHILD_MAPPER = FieldMappers.of( Child.class, ChildDTO.class )
                .add( "name", FAS, Child.FAS )
                .add( "age", FAS, Child.FAS )
                .add( "parent1name", FAS, "parent1", Child.FAS, ChildDTO::extractParentName )
                .add( "parent2name", FAS, "parent2", Child.FAS, ChildDTO::extractParentName );

        private static String extractParentName( Parent parent ) {
            return (parent == null) ? null : parent.name();
        }

        private String name;
        private Integer age;
        private String parent1name;
        private String parent2name;

        public ChildDTO() {
        }

        public ChildDTO( Child child ) {
            FROM_CHILD_MAPPER.map( child, this );
        }

        public ChildDTO( ChildDTO child ) {
            FAS.populateUs( this, child );
        }

        @Override
        public String toString() {
            return new ToStringBuilder().addAll( this, FAS ).toString();
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
            return FAS.equalInstancesWithEqualTypes( this, o ); // ChildDTO.equals(SubChildDTO) -> false
        }

        @Override
        public int hashCode() {
            return FAS.hashCodeFrom( this );
        }
    }

    private static final class SubChildDTO extends ChildDTO {
        public SubChildDTO( ChildDTO child ) {
            FAS.populateUs( this, child );
        }

        @Override
        @SuppressWarnings("com.haulmont.jpb.EqualsDoesntCheckParameterClass")
        public boolean equals( Object o ) {
            return FAS.equalInstancesWithSubTypes( this, o ); // SubChildDTO.equals(ChildDTO) -> true
        }
    }
}