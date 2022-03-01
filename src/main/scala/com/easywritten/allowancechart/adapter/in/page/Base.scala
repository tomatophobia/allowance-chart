package com.easywritten.allowancechart.adapter.in.page

import org.http4s.Uri
import scalatags.Text._
import scalatags.Text.all._

trait Base {
  def layout(pageTitle: String)(pageContent: Frag): TypedTag[String] =
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
                a(href := "index3.html", cls := "nav-link")("Home")
              ),
              li(cls := "nav-item d-none d-sm-inline-block")(
                a(href := "#", cls := "nav-link")("Contact")
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
                src := "dist/img/AdminLTELogo.png",
                alt := "AdminLTE Logo",
                cls := "brand-image img-circle elevation-3",
                style := "opacity: .8"
              ),
              span(cls := "brand-text font-weight-light")("AdminLTE 3")
            ),
            div(cls := "sidebar")(
              div(cls := "user-panel mt-3 pb-3 mb-3 d-flex")(
                div(cls := "image")(
                  img(src := "dist/img/user2-160x160.jpg", cls := "img-circle elevation-2", alt := "User Image")
                ),
                div(cls := "info")(
                  a(href := "#", cls := "d-block")("Alexander Pierce")
                )
              ),
              tags2.nav(cls := "mt-2")(
                ul(
                  cls := "nav nav-pills nav-sidebar flex-column",
                  data.widget := "treeview",
                  role := "menu",
                  data.accordion := "false"
                )(
                  li(cls := "nav-item")(
                    a(href := "#", cls := "nav-link")(
                      i(cls := "nav-icon fas fa-th"),
                      p(
                        "Simple Link",
                        span(cls := "right badge badge-danger")("New")
                      )
                    )
                  )
                )
              )
            )
          ),
          div(cls := "content-wrapper", style := "min-height: 895px;")(
            div(cls := "content-header")(
              div(cls := "container-fluid")(
                div(cls := "row mb-2")(
                  div(cls := "col-sm-6")(
                    h1(cls := "m-0")("Starter Page")
                  ),
                  div(cls := "col-sm-6")(
                    ol(cls := "breadcrumb float-sm-right")(
                      li(cls := "breadcrumb-item")(a(href := "#")("Home")),
                      li(cls := "breadcrumb-item active")("Starter Page")
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
            div(cls := "float-right d-none d-sm-inline")("Anything you want"),
            strong("Copyright © 2014-2021", a(href := "https://adminlte.io")("AdminLTE.io"), "."),
            "All rights reserved."
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

  private val webjars: Uri = Uri.unsafeFromString("/assets/webjars")
  def adminLTE(dir: String): Uri = (webjars / "AdminLTE" / "3.2.0" / ".").resolve(Uri.unsafeFromString(dir))
  val plotly: Uri = webjars / "plotly.js-dist-min" / "2.9.0" / "plotly.min.js"

  implicit val http4sUriAttrValue: scalatags.Text.AttrValue[Uri] = new scalatags.Text.GenericAttr[Uri]
}
