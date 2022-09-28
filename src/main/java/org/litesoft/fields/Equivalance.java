package org.litesoft.fields;

import java.util.List;
import java.util.Objects;

import org.litesoft.utils.HashCode;

@SuppressWarnings("unused")
public class Equivalance {
    public static <T> int absoluteHash( T us, FieldAccessors<T> fas ) {
        return mostlyHash( us, fas, 0 );
    }

    public static <T> int mostlyHash( T us, FieldAccessors<T> fas, int fromFasIndex ) {
        HashCode.Builder hb = HashCode.from( 0 );
        if ( us != null ) {
            List<Accessor<T, ?>> all = fas.getAll();
            for ( int i = fromFasIndex; i < all.size(); i++ ) {
                Accessor<T, ?> accessor = all.get( i );
                hb = hb.and( accessor.getValue( us ) );
            }
        }
        return hb.toHashCode();
    }

    public static <T> boolean sameTypes( T us, T them ) {
        if ( us == them ) {
            return true; // identity
        }
        return (us != null) && (them != null) && (us.getClass() == them.getClass());
    }

    public static <T> boolean absolute( T us, T them, FieldAccessors<T> fas ) {
        return absolute( sameTypes( us, them ), us, them, fas );
    }

    public static <T> boolean absolute( boolean sameTypes, T us, T them, FieldAccessors<T> fas ) {
        return mostly( sameTypes, us, them, fas, 0 );
    }

    public static <T> boolean mostly( T us, T them, FieldAccessors<T> fas, int fromFasIndex ) {
        return mostly( sameTypes( us, them ), us, them, fas, fromFasIndex );
    }

    public static <T> boolean mostly( boolean sameTypes, T us, T them, FieldAccessors<T> fas, int fromFasIndex ) {
        if ( !sameTypes || (us == null) || (them == null) ) { // Note: for this situation (null != null)!
            return false;
        }
        List<Accessor<T, ?>> all = fas.getAll();
        for ( int i = fromFasIndex; i < all.size(); i++ ) {
            Accessor<T, ?> accessor = all.get( i );
            if ( !Objects.equals( accessor.getValue( us ), accessor.getValue( them ) ) ) {
                return false;
            }
        }
        return true;
    }
}
