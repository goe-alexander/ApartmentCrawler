package crawler.entities;

import crawler.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull
    @Column(nullable = false, unique = true)
    private String externalId;
    @NonNull
    @Column
    private String source;
    @Column(length = 10485760)
    private String directLink;
    @Column(length = 10485760)
    private String title;
    @Column(length = 10485760)
    private String details;
    @Column
    private Integer rooms;
    @Column
    private Double builtSurface;
    @Column
    private Integer yearBuilt;
    @Column
    private Boolean inConstruction;
    @Column
    private Double price;
    @Column
    private String floor;
    //TBD: If necessary at all
    //private Integer numberOfFloors;
    @Column
    private Integer bathrooms;
    @Column
    private Boolean hasStorageSpace;
    @Column
    private StorageType storageSpaceType;
    @Column
    private Integer balconies;
    @Column
    private Boolean hasParkingSpot;
    @Column
    private Double lat;
    @Column
    private Double lon;
    @Column(length = 10485760)
    private String address;
    @Column
    private Date lastUpdated;
    @Column
    private Date dateCreated;

    @Version
    private Integer version;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CONTACT_ID", nullable = true)
    private Contact contact;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "apartment", orphanRemoval = true)
    private List<PriceHistory> priceHistories;

    @Override
    public String toString() {
        return title +
                "\n price: " + price +
                "\n yearBuilt: " + yearBuilt +
                "\n bathrooms: " + bathrooms +
                "\n rooms: " + rooms +
                "\n balconies: " + balconies +
                "\n hasParkingSport: " + hasParkingSpot +
                "\n floor: " + floor;
    }
}
