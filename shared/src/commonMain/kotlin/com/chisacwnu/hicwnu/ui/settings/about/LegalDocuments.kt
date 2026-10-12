/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.about

data class LegalDoc(val sections: List<LegalSection>)

data class LegalSection(
    val title: String? = null,
    val items: List<LegalItem>
)

sealed interface LegalItem {
    data class Para(val text: String) : LegalItem
    data class Bullet(val text: String) : LegalItem
    data class Component(
        val name: String,
        val license: String,
        val purpose: String
    ) : LegalItem
}

object LegalDocuments {

    val privacyPolicy: LegalDoc = LegalDoc(
        sections = listOf(
            LegalSection(
                items = listOf(
                    LegalItem.Para("版本1.0"),
                    LegalItem.Para("生效日期：2026年9月10日"),
                    LegalItem.Para("三句话版本"),
                    LegalItem.Bullet("这个App只在“登录教务系统导课表”时联网。导完之后你怎么断网都不影响使用。"),
                    LegalItem.Bullet("它没有服务器。学号、密码、课表都不会传到任何地方，数据只会在你设备上。"),
                    LegalItem.Bullet("没有广告、没有统计、没有推送SDK、没有任何收费。"),
                    LegalItem.Para("以下是把这三句话展开的细节。如果你只想知道结论，上面三条就够了。"),
                )
            ),
            LegalSection(
                title = "一、这份文档管什么、不管什么",
                items = listOf(
                    LegalItem.Para("管：安卓（Android）应用“Hi CWNU”以及它的网站。"),
                    LegalItem.Para("不管："),
                    LegalItem.Bullet("你手机系统自己的行为，比如系统的云备份、系统日历、系统通知；"),
                    LegalItem.Bullet("学校教务系统自身的记录行为；"),
                    LegalItem.Bullet("你在应用里点出去打开的别人的网页。"),
                    LegalItem.Para("写这份文档的人：CHISA_CWNU，一个西华师范大学商学院2024级的学生，不是公司、不是团队。"),
                )
            ),
            LegalSection(
                title = "二、你的信息在应用里会经过哪些地方",
                items = listOf(
                    LegalItem.Para("按你在应用里做的事，一种一种说清楚："),
                    LegalItem.Para("导入课表时，应用会碰到你的学号、密码、登录会话，以及教务系统返回的课表数据；其中密码只存在于这一次的内存里，课表则落在手机本地。这一步是唯一需要联网的。"),
                    LegalItem.Para("看课表或改课表时，会涉及课程名、教师、教室、节次、周次，以及你自己添加的课和备注，这些信息都保存在手机本地的数据库里，不联网。"),
                    LegalItem.Para("上课提醒会用到上课时间和课程名，它们留在手机本地，交给系统的闹钟与通知去执行，不联网。"),
                    LegalItem.Para("同步到系统日历时，上课时间、地点、课程名会被写进你的系统日历，不联网。"),
                    LegalItem.Para("导出课表（文件或图片）时，课表内容会被保存到你自己在文件选择器里挑的那个位置，不联网。"),
                    LegalItem.Para("更换课表背景图时，应用会把你挑中的那一张图片复制一份进来，存在手机本地，不联网。"),
                    LegalItem.Para("也就是说，除了导入课表那一项，其他每一项都不需要网络。"),
                )
            ),
            LegalSection(
                title = "三、联网：只有一次，之后的次数是零",
                items = listOf(
                    LegalItem.Bullet("你点下“教务系统导入”以后，应用内浏览器会按顺序走完这段路：authserver.cwnu.edu.cn（统一身份认证）→ehall.cwnu.edu.cn（信息门户）→jwxt.cwnu.edu.cn（教务系统，课表数据在这里）。"),
                    LegalItem.Bullet("这几个页面里跑着一段我写好的适配脚本，替你填账号、点登录、翻页面、把课表抓出来。其中有几个课表请求由应用直接发出，但时机只在这段导入流程里，地址也只限于上面这三个域名。"),
                    LegalItem.Bullet("你退出导入页面，就是这次联网的结束。之后应用不会再发任何请求：没有后台轮询、没有心跳包、没有静默更新检查。"),
                    LegalItem.Bullet("三件必须如实讲的事：第一，导入的时候，学校网页可能自己去加载第三方资源（字体、CDN之类），那是学校页面的行为，我控制不了，也不参与；第二，你在这些网页里的操作记录，由学校系统按它自己的规则保存，那是学校的事；第三，“关于软件”页面里有几个按钮会跳到学校官网、本软件官网和我的主页，那些是用系统浏览器打开的别人的网站，跳过去以后适用对方的规则。"),
                    LegalItem.Para("你可以自己验证，欢迎验证："),
                    LegalItem.Bullet("导完课表后打开飞行模式——看课表、改课表、提醒、小组件都照常，因为根本不联网。"),
                    LegalItem.Bullet("在系统设置里把本应用的联网权限关掉——一样能用，只是不能再导入，因为根本不联网。"),
                    LegalItem.Bullet("如果你实在不信就抓包：你应该只会看到www.cwnu.edu.cn相关的域名。"),
                )
            ),
            LegalSection(
                title = "四、学号和密码的一生",
                items = listOf(
                    LegalItem.Para("按时间顺序说，一条不省："),
                    LegalItem.Para("当你点“执行导入”的时候，应用会弹出原生输入框（不跳网页），两个框：学号、密码；你填完点确定，这两个值就直接交给页面里的脚本，填进学校登录表单，而加密和提交都是学校登录页自己做的；登录进行中，密码只存在于这一次的内存里，不写数据库、不写文件、不写日志、不做自动填充，也没有“记住密码”这个功能（但是有的手机系统有密码存储功能）；登录成功后，密码就已经被丢弃了，之后靠学校下发的临时Cookie维持这次会话；等你退出导入页面时，浏览器实例被销毁，Cookie、缓存、SSL状态、表单数据一并清除，应用里不留任何登录痕迹；以后再要导入，就得重新输入一遍，就是这样。"),
                    LegalItem.Para("补三句："),
                    LegalItem.Bullet("应用的运行日志里只有“页面元素找到了几个”这类排障信息，我看不到你的密码，因为它压根没被打印。"),
                    LegalItem.Bullet("除了学校那三个域名，没有第二个接收方——我没有服务器可以用来接收它。"),
                    LegalItem.Bullet("唯一我挡不住的情况：你装的是别人改过的安装包。请只从官方渠道下载，详见《服务条款》第四条。"),
                )
            ),
            LegalSection(
                title = "五、留在你手机里的东西",
                items = listOf(
                    LegalItem.Para("全部在本地，没有云端，没有同步。"),
                    LegalItem.Para("课表，包括课程名、教师、教室、节次、周次、学期起止日期，存在应用私有目录的本地数据库里；想删掉它，可以在应用内删除课程、清空数据，或者直接卸载应用。"),
                    LegalItem.Para("你自己加的东西，比如自定义课程、备注、颜色标记，也存在同一个本地数据库里，删除方式和上面一样。"),
                    LegalItem.Para("设置，包括语言、主题、夏冬作息、总周数、起始星期、提醒设置等，存在应用的本地偏好存储里，你可以在设置页里改，或者卸载应用。"),
                    LegalItem.Para("图片，也就是你选的背景图（应用复制了一份进来），存在应用私有目录，换一张或者卸载应用就没有了。"),
                    LegalItem.Para("顺带说清权限边界：应用不申请存储权限。导入课表文件、导出课表文件、挑背景图，全都走系统的文件选择器/照片选择器——你选中哪个文件，应用就只能碰哪一个，看不到你手机里其他的东西。"),
                )
            ),
            LegalSection(
                title = "六、我申请了哪些权限",
                items = listOf(
                    LegalItem.Bullet("网络（INTERNET）：只用于导入课表时连接学校教务系统，在安装时声明。导入要用，其余时间用不上。"),
                    LegalItem.Bullet("网络状态（ACCESS_NETWORK_STATE）：用来判断当前能不能联网，导入失败时好给你提示，在安装时声明。不给也行。"),
                    LegalItem.Bullet("通知（POST_NOTIFICATIONS）：用于上课提醒，在你第一次开启提醒时向你索取。不给也行，只是没有提醒。"),
                    LegalItem.Bullet("精确闹钟（SCHEDULE_EXACT_ALARM）：让提醒在设定的那一分钟响，需要你去系统设置里授权。不授权也行，只是提醒可能不准时。"),
                    LegalItem.Bullet("勿扰访问（ACCESS_NOTIFICATION_POLICY）：上课时自动静音（默认关闭，你主动开启才生效），开启时会跳到系统设置。不给也行。"),
                    LegalItem.Bullet("开机自启（RECEIVE_BOOT_COMPLETED）：手机重启后恢复提醒和桌面小组件，在安装时声明。不给也行，只是重启后要手动打开一次应用。"),
                    LegalItem.Bullet("日历读写（READ/WRITE_CALENDAR）：把课表写进系统日历，只在你点击同步时向你索取。不给也行。"),
                    LegalItem.Para("没有申请、也不会申请的：存储/相册权限、位置、通讯录、短信、通话记录、相机、麦克风，以及任何用来追踪你的设备标识（IMEI、MAC地址、Android ID、OAID等）。"),
                    LegalItem.Para("除导入必需的网络权限外，任何一项权限被拒绝，都只影响对应功能，不影响你看课表。"),
                )
            ),
            LegalSection(
                title = "七、我不做的事",
                items = listOf(
                    LegalItem.Bullet("不收集设备标识、不做用户画像、不做个性化推荐、不做广告投放；"),
                    LegalItem.Bullet("不读通讯录、短信、通话记录、位置、相册、已安装应用列表；"),
                    LegalItem.Bullet("不做使用统计、行为埋点、崩溃上报、性能上报；"),
                    LegalItem.Bullet("不做版本检查、不做静默更新、不请求任何更新接口；"),
                    LegalItem.Bullet("不需要注册，不收手机号、不收邮箱、不收证件信息；"),
                    LegalItem.Bullet("没有广告组件，没有内购，没有付费点；"),
                    LegalItem.Bullet("不会把你的课表拿去分析、训练模型或变现。"),
                )
            ),
            LegalSection(
                title = "八、什么情况下信息会离开手机",
                items = listOf(
                    LegalItem.Para("只有这四种："),
                    LegalItem.Bullet("你自己动手：导出课表文件、导出课表图片、分享给同学、同步到系统日历。存哪、发给谁，你说了算。"),
                    LegalItem.Bullet("你选的那个位置：导出时你在文件选择器里挑的文件夹或网盘。到了那里之后，适用那个服务的规则。"),
                    LegalItem.Bullet("学校教务系统：登录和读取课表时，请求发向学校系统，由学校按其规则处理。"),
                    LegalItem.Bullet("法律要求：依法定程序，应司法机关、行政机关的要求。"),
                    LegalItem.Para("没有第五种。我不卖、不租、不换你的信息。"),
                )
            ),
            LegalSection(
                title = "九、手机系统这一层，有两件事你要知道",
                items = listOf(
                    LegalItem.Bullet("系统云备份：应用清单里允许了Android的自动备份。也就是说，在开了云备份或换机迁移的设备上，本地课表数据可能被系统一起备份到你自己的云账号，或跟着换机迁移到新手机。这件事由手机厂商和操作系统执行，数据在你自己的云账号里，我碰不到也不参与。不想这样的话，去系统设置里关掉本应用的自动备份，或换机时不迁移它的数据。数据真丢了也不要紧，重新导入一次课表就回来了。"),
                    LegalItem.Bullet("学校的日志：导入过程中产生的访问记录，学校系统那边会有，那是学校的事。"),
                )
            ),
            LegalSection(
                title = "十、安全这件事，我做到哪一步、做不到哪一步",
                items = listOf(
                    LegalItem.Para("做到的："),
                    LegalItem.Bullet("与学校系统之间全程HTTPS；"),
                    LegalItem.Bullet("密码零落地，用完即清；"),
                    LegalItem.Bullet("课表放在应用私有目录，普通应用直接读不到；"),
                    LegalItem.Bullet("联网范围收口在导入这一个流程里，没有常驻后台请求。"),
                    LegalItem.Para("做不到的（也提醒你）："),
                    LegalItem.Bullet("本地数据库里的课表没有做额外加密。设备被Root/越狱，或者屏幕被别人看到，课表内容就可能被看到。"),
                    LegalItem.Bullet("课表截图里有你的班级、教室、教师，往群里发之前先想一想。"),
                    LegalItem.Bullet("别人二次打包的安装包，我完全无能为力。"),
                    LegalItem.Para("如果将来真的发生（或可能发生）信息泄露，我会通过官方渠道公告或邮件告诉你，并向主管部门报告。"),
                )
            ),
            LegalSection(
                title = "十一、未成年人",
                items = listOf(
                    LegalItem.Para("本应用面向高校在校学生，使用者通常已成年。不满14周岁请不要使用。未满18周岁请在监护人陪同下阅读本政策，并取得监护人同意后使用。我不会针对未成年人做推送、营销，也不会额外收集他们的信息。"),
                )
            ),
            LegalSection(
                title = "十二、你可以随时做的事",
                items = listOf(
                    LegalItem.Bullet("想改、想补：在应用里编辑课程、备注和设置。"),
                    LegalItem.Bullet("想删：删单门课、清空课表数据、在系统里清除应用数据、卸载应用，四种都有效。"),
                    LegalItem.Bullet("想撤回同意：在系统设置里关掉某项权限（通知/日历/联网），或者直接卸载；撤回不影响撤回之前已经进行的处理。"),
                    LegalItem.Bullet("想导出带走：用课程文件导出、日历文件导出。"),
                    LegalItem.Para("不会给你设门槛，也不会因为你行使这些权利就降低服务质量。另外实话实说：数据本来就在你手里，所以“删除”这件事你自己动手最快、最彻底。"),
                )
            ),
            LegalSection(
                title = "十三、这份文档怎么变",
                items = listOf(
                    LegalItem.Para("改动会更新顶部的版本号与生效日期。"),
                )
            ),
            LegalSection(
                title = "十四、找我",
                items = listOf(
                    LegalItem.Bullet("微信：Vivian1014_TIAN（田曦薇生日是10月14哦）"),
                    LegalItem.Bullet("QID：Deep1Sleep（QQ号：2997339738）"),
                    LegalItem.Bullet("Gitee：https://gitee.com/cwnu6（GitHub不建议）"),
                    LegalItem.Bullet("官方网站：https://chisa8cwnu.github.io/scheduleforCWNU/"),
                    LegalItem.Para("对答复不满意的话，你就认了吧。"),
                    LegalItem.Para("写这份东西的是CHISA_CWNU，不是律师，所以这里没有绕人的句式。如果你发现应用的实际行为跟这份文档对不上，请以实际行为为准，并且一定告诉我。"),
                )
            ),
        )
    )

    val serviceTerms: LegalDoc = LegalDoc(
        sections = listOf(
            LegalSection(
                items = listOf(
                    LegalItem.Para("版本1.0"),
                    LegalItem.Para("生效日期：2026年9月10日"),
                )
            ),
            LegalSection(
                title = "一、先说清楚这份东西是什么",
                items = listOf(
                    LegalItem.Para("你和作者CHISA_CWNU之间的一份约定。你装上了、打开了、开始用了，就等于你接受它。不接受的话，卸载就好，不需要告诉我理由。"),
                    LegalItem.Para("未满18周岁的，请让监护人陪你一起看一遍；你自己用它的行为，也应该符合学校的管理规定。"),
                )
            ),
            LegalSection(
                title = "二、这是谁做的软件，以及它不是什么",
                items = listOf(
                    LegalItem.Para("谁做的：一个在西华师范大学商学院修炼的2024级学生（CHISA_CWNU），课余时间写的，一个人维护。"),
                    LegalItem.Para("它不是什么（这几条比“它是什么”更重要）："),
                    LegalItem.Bullet("不是学校官方应用！和学校教务部门、网络与信息化管理中心没有任何隶属、授权或合作关系，不代表学校，也不会以学校的名义发言。"),
                    LegalItem.Bullet("不是商业产品。不卖钱、不接广告、没有公司，不靠它盈利。"),
                    LegalItem.Bullet("不是选课工具。不抢课、不刷课、不代查成绩、不查考试安排、不替任何人报名。它只做一件事：把你自己在教务系统里的课表“搬”到手机上，比教务系统网页好看好用方便。"),
                    LegalItem.Bullet("不是课表数据的主人。课表、成绩、学籍都属于西华师范大学及其权利人。一切以教务系统发布的正式数据为准——培养方案调整、临时调课、换教室、系统维护都可能让它和你手机上显示的不一样，遇到不一致，以教务系统为准并重新导入一次。"),
                )
            ),
            LegalSection(
                title = "三、关于免费，我把话说死",
                items = listOf(
                    LegalItem.Bullet("永久免费、无广告、无内购、无会员、无付费点。不会用“解锁功能”“去广告”“加速导入”之类的名目收你的钱，也不接受打赏、充值、赞助换特权。"),
                    LegalItem.Bullet("作者不通过这个应用获得任何收入，也不会把你的课表数据卖给别人，因为数据压根没离开过你的手机。"),
                    LegalItem.Bullet("如果你看到以它名义收费、要求充值、插广告的版本，那一定是假冒的，这种行为太司马了。请你立刻停用，并把来源告诉我。"),
                )
            ),
            LegalSection(
                title = "四、安装包从哪拿",
                items = listOf(
                    LegalItem.Bullet("官方网站https://chisa8cwnu.github.io/scheduleforCWNU/、作者在学校QQ频道/小红书等平台发布的帖子。"),
                    LegalItem.Bullet("它不上架应用商店，也不做应用内更新。有新版本需要你自己下载安装包，覆盖安装。"),
                    LegalItem.Bullet("从别的渠道拿的、或者装了别人动过手脚的包，很可能就是为了偷你的学号密码。这种后果只能你自己承担，我既没有能力也没有办法去防。"),
                    LegalItem.Bullet("装的时候遇到“风险提示”是正常的：它没上架、没备案、签名是自签的。但请注意，“有风险提示”和“来源可疑”是两件事，你得确定是从正规渠道来的，再谈安装。"),
                )
            ),
            LegalSection(
                title = "五、你的学号密码",
                items = listOf(
                    LegalItem.Bullet("导入课表需要你输入教务系统的学号和密码。它只在这一次的登录过程中使用，不保存、不落盘、不上传，你退出导入页面时连同Cookie一起清掉。详见《隐私政策》第四节。"),
                    LegalItem.Bullet("请自己保管好账号密码：不要借人，不要交给任何声称“代导入课表”的人。因为你自己泄露出去的，后果由你承担。"),
                    LegalItem.Bullet("你承诺：用它登录的行为符合学校规定，只查本人的课表。"),
                )
            ),
            LegalSection(
                title = "六、用它的规矩",
                items = listOf(
                    LegalItem.Para("不要做下面这些事："),
                    LegalItem.Bullet("高频、批量、自动化地请求学校教务系统，或者用爬虫、压测、反复循环重试去干扰它正常运行；"),
                    LegalItem.Bullet("查询、抓取、保存、传播别人的课表、成绩、学籍；"),
                    LegalItem.Bullet("拿课表数据去做生意：转卖、二次分发、建一个跟学校系统竞争的服务；"),
                    LegalItem.Bullet("反编译、改一改，然后打着“官方版”“增强版”的名义发布、宣传或收费，或者冒用学校、本应用的名义；"),
                    LegalItem.Bullet("重新打包安装包到处分发；"),
                    LegalItem.Bullet("拿它做违法的事——违反中国大陆的法律、危害网络安全、侵害他人权益，都不行。"),
                    LegalItem.Para("违反了造成损害，责任你自己扛；给本软件、学校或第三方造成损失，你要赔。"),
                )
            ),
            LegalSection(
                title = "七、东西归谁",
                items = listOf(
                    LegalItem.Bullet("课表与教务数据：学校的。本应用只做技术性的获取与本地展示，不主张任何权利，你也不会因为用了它而获得对这类数据的权利。"),
                    LegalItem.Bullet("你自己建的内容：手动添加的课程、备注、颜色，归你。"),
                    LegalItem.Bullet("名字和标识：“Hi CWNU”“CHISA_CWNU”这几个名称与标识不在开源许可的范围里。二次分发时，别用同名或近似的名字去混淆，也别暗示我给你的版本背过书。"),
                )
            ),
            LegalSection(
                title = "八、请别只靠它（重要，真的）",
                items = listOf(
                    LegalItem.Bullet("提醒不一定叫得醒你。提醒和桌面小组件依赖安卓（Android）系统的闹钟和省电策略，手机的省电模式、厂商的后台清理、权限被系统回收，都可能让提醒迟到甚至不响。重要的事情请自己再盯一眼，不要把提醒功能当成唯一的依靠。"),
                    LegalItem.Bullet("它显示的不一定是最新的。课表以教务系统为准；发现不对就重新导入。"),
                    LegalItem.Bullet("教务系统一改版，导入就可能失效。这时需要等新版适配。学校系统的改动我提前不知道，也无法承诺什么时候修好。"),
                    LegalItem.Bullet("它还可能受你手机本身的影响：清理数据、刷机、恢复出厂设置、换机没迁移，课表都会没。重新导入即可。"),
                )
            ),
            LegalSection(
                title = "九、出了事我赔到什么程度",
                items = listOf(
                    LegalItem.Para("本应用以“按原样（ASIS）”提供，不附带任何明示或默示的担保，包括适销性、特定用途适用性、不侵权在内的各种担保都不承诺。"),
                    LegalItem.Bullet("课表数据出错、延迟、遗漏，导致你缺课、误考、成绩或学业受影响——不赔。"),
                    LegalItem.Bullet("学校系统维护、升级、改版、限流导致导入失败或功能失效——不赔。"),
                    LegalItem.Bullet("你的设备故障、刷机、误删、系统升级导致数据丢失——不赔。"),
                    LegalItem.Bullet("你装了非官方渠道或被二次打包的安装包导致的一切损失——不赔。"),
                    LegalItem.Bullet("提醒没响、小组件没刷新导致你迟到——不赔（见第八条）。"),
                    LegalItem.Para("在法律允许的最大范围内，我对你的赔偿总额不超过你为使用本应用实际支付的费用——本应用免费，所以这个上限是“0”。"),
                    LegalItem.Para("另外请理解：这是个人课余项目，我不承诺持续的维护节奏、响应速度或功能路线图。我会尽力修bug，但不能保证什么时候。"),
                )
            ),
            LegalSection(
                title = "十、功能会变，也可能停",
                items = listOf(
                    LegalItem.Para("因为教务系统接口变了、学校不让我做这个、技术上要重构，或者单纯因为我忙起来了，甚至是毕业了，部分乃至全部功能都可能被修改、暂停或终止。重大变化我会尽量在官方渠道提前说一声。"),
                    LegalItem.Para("你可以随时卸载来终止使用，卸载就算作你要删掉本地数据的动作。条款终止后，第七条（归属）、第八条（别只靠它）、第九条（赔偿上限）、第十一条（法律与争议）继续有效。"),
                )
            ),
            LegalSection(
                title = "十一、法律与争议",
                items = listOf(
                    LegalItem.Para("这份条款适用中华人民共和国法律（不含冲突规范）。有分歧先好好谈；谈不拢，任何一方都可以向有管辖权的人民法院起诉。"),
                )
            ),
            LegalSection(
                title = "十二、条款会更新，以及怎么找我",
                items = listOf(
                    LegalItem.Para("条款可能会随功能调整而修订，修订后更新顶部的版本号和生效日期。你继续用就视为接受；不接受就卸载。"),
                    LegalItem.Bullet("微信：Vivian1014_TIAN（田曦薇生日是10月14哦）"),
                    LegalItem.Bullet("QID：Deep1Sleep（QQ号：2997339738）"),
                    LegalItem.Bullet("Gitee：https://gitee.com/cwnu6（GitHub不建议）"),
                    LegalItem.Bullet("官方网站：https://chisa8cwnu.github.io/scheduleforCWNU/"),
                    LegalItem.Para("咨询、意见、投诉，我有空会尽快回你。"),
                    LegalItem.Para("谢谢你把课表交给它。"),
                )
            ),
        )
    )
}
