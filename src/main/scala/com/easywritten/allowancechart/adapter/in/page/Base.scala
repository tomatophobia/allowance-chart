package com.easywritten.allowancechart.adapter.in.page

import cats.implicits._
import org.http4s.Uri
import scalatags.Text._
import scalatags.Text.all._

trait Base {
  // pageContent 입력 가독성을 높이기 위해서 currying
  def layout(pageTitle: String, currentMenu: Menu)(pageContent: Frag): TypedTag[String] =
    html(
      head(
        meta(charset := "utf-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1"),
        tags2.title(pageTitle),
        link(
          rel := "stylesheet",
          href := "https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,400i,700&display=fallback"
        ),
        link(
          rel := "stylesheet",
          href := adminLTE("plugins/fontawesome-free/css/all.min.css")
        ),
        link(
          rel := "stylesheet",
          href := adminLTE("dist/css/adminlte.min.css")
        ),
        link(
          rel := "stylesheet",
          href := adminLTE("plugins/sweetalert2-theme-bootstrap-4/bootstrap-4.min.css")
        ),
        link(
          rel := "stylesheet",
          href := adminLTE("plugins/toastr/toastr.min.css")
        ),
        tags2.style(".fas { line-height: inherit; }")
      ),
      body(cls := "hold-transition sidebar-mini")(
        div(cls := "wrapper")(
          tags2.nav(cls := "main-header navbar navbar-expand navbar-white navbar-light")(
            ul(cls := "navbar-nav")(
              li(cls := "nav-item")(
                a(cls := "nav-link", data.widget := "pushmenu", href := "#", role := "button")(
                  i(cls := "fas fa-bars")
                )
              ),
              li(cls := "nav-item d-none d-sm-inline-block")(
                a(href := "/home", cls := "nav-link")("Home")
              )
            ),
            ul(cls := "navbar-nav ml-auto")(
              li(cls := "nav-item")(
                a(cls := "nav-link", data.widget := "navbar-search", href := "#", role := "button")(
                  i(cls := "fas fa-search")
                ),
                div(cls := "navbar-search-block")(
                  form(cls := "form-inline")(
                    div(cls := "input-group input-group-sm")(
                      input(
                        cls := "form-control form-control-navbar",
                        tpe := "search",
                        placeholder := "Search",
                        aria.label := "Search"
                      ),
                      div(cls := "input-group-append")(
                        button(cls := "btn btn-navbar", tpe := "submit")(
                          i(cls := "fas fa-search")
                        ),
                        button(cls := "btn btn-navbar", tpe := "button", data.widget := "navbar-search")(
                          i(cls := "fas fa-times")
                        )
                      )
                    )
                  )
                )
              ),
              li(cls := "nav-item")(
                a(
                  cls := "nav-link",
                  data.widget := "control-sidebar",
                  data.slide := "true",
                  href := "#",
                  role := "button"
                )(
                  i(cls := "fas fa-th-large")
                )
              )
            )
          ),
          tags2.aside(cls := "main-sidebar sidebar-dark-primary elevation-4")(
            a(href := "index3.html", cls := "brand-link")(
              img(
                src := "/assets/images/scala-logo.png",
                alt := "Scala Logo",
                cls := "brand-image img-circle elevation-3",
                style := "opacity: .8"
              ),
              span(cls := "brand-text font-weight-light")("Allowance Chart")
            ),
            div(cls := "sidebar")(
              div(cls := "user-panel mt-3 pb-3 mb-3 d-flex")(
                div(cls := "image")(
                  img(src := "/assets/images/totoro.png", cls := "img-circle elevation-2", alt := "User Image")
                ),
                div(cls := "info")(
                  a(href := "#", cls := "d-block")("Youngseo Choi")
                )
              ),
              tags2.nav(cls := "mt-2")(
                ul(
                  cls := "nav nav-pills nav-sidebar flex-column",
                  data.widget := "treeview",
                  role := "menu",
                  data.accordion := "false"
                )(
                  sideMenu(currentMenu),
                )
              )
            )
          ),
          div(cls := "content-wrapper", style := "min-height: 895px;")(
            div(cls := "content-header")(
              div(cls := "container-fluid")(
                div(cls := "row mb-2")(
                  div(cls := "col-sm-6")(
                    h1(cls := "m-0")(pageTitle)
                  ),
                  div(cls := "col-sm-6")(
                    ol(cls := "breadcrumb float-sm-right")(
                      li(cls := "breadcrumb-item")(a(href := "#")("Home")),
                      li(cls := "breadcrumb-item active")(currentMenu.name)
                    )
                  )
                )
              )
            ),
            div(cls := "content")(
              div(cls := "container-fluid")(pageContent)
            )
          ),
          tags2.aside(cls := "control-sidebar control-sidebar-dark", style := "display: none;")(
            div(cls := "p-3")(
              h5("Title"),
              p("Sidebar content")
            )
          ),
          footer(cls := "main-footer")(
            div(cls := "float-right d-none d-sm-inline")(
              a(href := "https://github.com/tomatophobia/allowance-chart")("Github")
            ),
            strong("문의 사항은 ", a(href := "mailto:ys550499@gmail.com")("이메일로")),
            " 주시면 감사드리겠습니다!"
          )
        ),
        script(src := adminLTE("plugins/jquery/jquery.min.js")),
        script(src := adminLTE("plugins/bootstrap/js/bootstrap.bundle.min.js")),
        script(src := adminLTE("dist/js/adminlte.min.js")),
        script(src := adminLTE("plugins/sweetalert2/sweetalert2.min.js")),
        script(src := adminLTE("plugins/toastr/toastr.min.js")),
        script(src := plotly)
      )
    )

  private def sideMenu(current: Menu): Seq[TypedTag[String]] = {
    Menu.values map { menu =>
      val active = if (menu === current) "active" else ""
      li(cls := "nav-item")(
        a(href := menu.link, cls := s"nav-link $active")(
          i(cls := "nav-icon fas fa-th"),
          p(
            menu.name
          )
        )
      )
    }
  }

  private val webjars: Uri = Uri.unsafeFromString("/assets/webjars")
  def adminLTE(dir: String): Uri = (webjars / "AdminLTE" / "3.2.0" / ".").resolve(Uri.unsafeFromString(dir))
  val plotly: Uri = webjars / "plotly.js-dist-min" / "2.9.0" / "plotly.min.js"

  implicit val http4sUriAttrValue: scalatags.Text.AttrValue[Uri] = new scalatags.Text.GenericAttr[Uri]
}
