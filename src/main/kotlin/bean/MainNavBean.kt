package bean

data class MainNavBean(val svgName: String, val labelText: String)

fun createMainNavData() = mutableListOf(
    MainNavBean("ic_app_info.svg","应用信息"),
    MainNavBean("ic_phone.svg","手机信息"),
    MainNavBean("ic_quick_future.svg","快捷功能"),
    MainNavBean("ic_folder.svg","文件管理"),
    MainNavBean("ic_phone_record.svg","设备历史"),
)