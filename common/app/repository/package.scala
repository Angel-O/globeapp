
package object repository {
  
  import apimodels.common.Entity
  import repository.GenericRepository
  import repository.SearchCriteria
  
  type Criteria = SearchCriteria
  type RepoBase[T <: Entity] = GenericRepository[T]
}