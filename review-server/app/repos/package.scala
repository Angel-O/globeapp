import apimodels.common.Entity


package object repos {
  import repository.GenericRepository
  import repository.SearchCriteria
  
  type Criteria = SearchCriteria
  type RepoBase[T <: Entity] = GenericRepository[T]
}