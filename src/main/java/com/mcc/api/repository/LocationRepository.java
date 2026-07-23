package com.mcc.api.repository;

import com.mcc.api.model.Location;
import com.mcc.api.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    @Query(value = "SELECT * FROM locations WHERE ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)", nativeQuery = true)
    List<Location> findWithinRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") double radiusMeters);

    @Query(value = "SELECT * FROM locations WHERE ST_Intersects(location::geometry, ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326))", nativeQuery = true)
    List<Location> findWithinBounds(@Param("minLat") double minLat, @Param("minLng") double minLng,
                                    @Param("maxLat") double maxLat, @Param("maxLng") double maxLng);

    @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Location> searchByName(@Param("search") String search);

    List<Location> findByServiceCategory(ServiceCategory category);

    @Query("SELECT DISTINCT l FROM Location l JOIN CardAcceptance ca ON ca.location = l WHERE ca.works = true")
    List<Location> findWithWorkingCards();

    @Query("SELECT l FROM Location l WHERE NOT EXISTS (SELECT 1 FROM CardAcceptance ca WHERE ca.location = l AND ca.works = true)")
    List<Location> findWithNoWorkingCards();

    @Query("SELECT DISTINCT l FROM Location l JOIN CardAcceptance ca ON ca.location = l WHERE ca.cardType = :cardType")
    List<Location> findByCardType(@Param("cardType") String cardType);

    @Query("SELECT DISTINCT l FROM Location l JOIN CardAcceptance ca ON ca.location = l WHERE ca.cardType = :cardType AND ca.works = :works")
    List<Location> findByCardTypeAndWorks(@Param("cardType") String cardType, @Param("works") Boolean works);
}