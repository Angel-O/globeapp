package utils

import config._
import router.HashUpdater
//import com.github.ghik.silencer.silent;

//TODO rather than hardcoding it take baseUrl from config or routes

// This is a util trait that allows to set the baseUrl once and reuse router
// HashUpdater.push method Note: it is ok to silent the complier because the
// baseUrl is used by a lazily evaluated method
//@silent 
trait HashChanger extends HashUpdater { val baseUrl = Some(ROOT_PATH) }
