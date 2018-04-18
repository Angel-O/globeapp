//package repos
//
//import scala.concurrent.ExecutionContext
//
//import com.github.dwickern.macros.NameOf._
//
//import javax.inject.Inject
//import play.api.libs.json.Json.obj
//import play.modules.reactivemongo.ReactiveMongoApi
//import repository.RepoBase
//import apimodels.mobile.MobileApp

//TODO this is not needed
//class AppSuggestionRepository @Inject()(implicit ec: ExecutionContext,
//                               reactiveMongoApi: ReactiveMongoApi)
//    extends RepoBase[MobileApp]("mobileapp-suggestion", ec, reactiveMongoApi)

//use http client to make request to other services...