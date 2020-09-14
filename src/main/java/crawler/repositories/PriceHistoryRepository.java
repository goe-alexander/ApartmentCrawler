package crawler.repositories;

import crawler.entities.PriceHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceHistoryRepository extends CrudRepository<PriceHistory, Long> {

}
