package components

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.Binding.BindingSeq

package object core {
  
  object Helpers{
    def toBindingSeq[T](elements: Seq[T]) = {
      var temp: Vars[T] = Vars.empty;
      elements.foreach(x => temp.value += x)
      var bindingElementsSeq: BindingSeq[T] = temp
      bindingElementsSeq
      }

    def toBindingSeq[T](elements: Option[T]) = {
      var temp: Vars[T] = Vars.empty;
      elements.foreach(x => temp.value += x)
      var bindingElementsSeq: BindingSeq[T] = temp
      bindingElementsSeq
    } 
  }
}