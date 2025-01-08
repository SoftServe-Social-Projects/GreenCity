package greencity.entity.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
@Data
public final class Address {
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String streetEn;

    @Column
    private String streetUa;

    @Column
    private String houseNumber;

    @Column
    private String cityEn;

    @Column
    private String cityUa;

    @Column
    private String regionEn;

    @Column
    private String regionUa;

    @Column
    private String countryEn;

    @Column
    private String countryUa;

    @Column
    private String formattedAddressEn;

    @Column
    private String formattedAddressUa;
}