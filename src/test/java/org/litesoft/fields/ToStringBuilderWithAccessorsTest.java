package org.litesoft.fields;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToStringBuilderWithAccessorsTest {

    @Test
    void test_methods() {
        String actual = new Child( "Pebbles", 1, new Parent( "Wilma" ), new Parent( "Fred" ) ).toString();
        assertEquals( String.join( "\n"
                              , "name: 'Pebbles'"
                              , "age: 1"
                              , "parent1:"
                              , "  name: 'Wilma'"
                              , "parent2:"
                              , "  name: 'Fred'" // No Newline
                      ).replace( '\'', '"' ),
                      actual );

        assertEquals( String.join( "\n",
                                   "name    String  (required)",
                                   "age     Integer",
                                   "parent1         (gender? & more metaData)",
                                   "parent2", // Newline!
                                   ""
        ), Child.FAS.toString() );
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

    private record Child(String name, int age, Parent parent1, Parent parent2) {
        static FieldAccessors<Child> FAS = new FieldAccessors<>();

        static {
            FAS.required( "name", Child::name ).withType( String.class );
            FAS.optional( "age", Child::age ).withType( Integer.class );
            FAS.optional( "parent1", Child::parent1 ).withMetaData( "gender?" ).addMetaData( "more metaData" );
            FAS.optional( "parent2", Child::parent2 );
        }

        @Override
        public String toString() {
            return new ToStringBuilder().addAll( this, FAS ).toString();
        }
    }
}