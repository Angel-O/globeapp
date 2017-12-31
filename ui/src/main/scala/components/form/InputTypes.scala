package components.form

sealed trait InputType{
  val name: String
}

case object TextInput extends InputType {val name = "text"}
case object EmailInput extends InputType {val name = "email"}
case object PasswordInput extends InputType {val name = "password"}
case object TextareaInput extends InputType {val name = "textarea"}
case object SelectInput extends InputType {val name = "select"} // unused...
case object CheckboxInput extends InputType {val name = "checkbox"}
case object RadioInput extends InputType {val name = "radio"}