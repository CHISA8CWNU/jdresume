
(async function () {
    "use strict";

    const presetTimeSlots = [
        { number: 1,  startTime: "08:00", endTime: "08:40" },
        { number: 2,  startTime: "08:50", endTime: "09:30" },
        { number: 3,  startTime: "09:45", endTime: "10:25" },
        { number: 4,  startTime: "10:35", endTime: "11:15" },
        { number: 5,  startTime: "11:25", endTime: "12:05" },
        { number: 6,  startTime: "15:00", endTime: "15:40" },
        { number: 7,  startTime: "15:50", endTime: "16:30" },
        { number: 8,  startTime: "16:40", endTime: "17:20" },
        { number: 9,  startTime: "17:30", endTime: "18:10" },
        { number: 10, startTime: "19:30", endTime: "20:10" },
        { number: 11, startTime: "20:20", endTime: "21:00" },
        { number: 12, startTime: "21:10", endTime: "21:50" }
    ];

    const JWXT_HOME = "https://jwxt.cwnu.edu.cn/student/home";
    const SCRIPT_VERSION = "v6.7";

    const bridge = window.hicwnuBridge || window.AndroidBridge;
    const bridgePromise = window.hicwnuBridgePromise || window.AndroidBridgePromise;

    function toast(msg) {
        if (bridge && bridge.showToast) bridge.showToast(msg);
        console.log("[CWNU][Toast]", msg);
    }

    function log() {
        console.log.apply(console, ["[CWNU]"].concat(Array.prototype.slice.call(arguments)));
    }

    function diagLog(label, value) {
        try {
            if (window._cwnuDiagBridge && window._cwnuDiagBridge.logData) {
                const s = (typeof value === "string") ? value : JSON.stringify(value);
                window._cwnuDiagBridge.logData(label, s || "(空)");
            }
        } catch (e) { log("diagLog 失败:", e); }
    }

    async function alertBox(title, content) {
        if (bridgePromise && bridgePromise.showAlert) {
            return await bridgePromise.showAlert(title, content, "知道了");
        }
        return true;
    }

    window.cwnuValidateUid = function (v) {
        if (!v || !v.trim()) return "学号不能为空";
        if (!/^[A-Za-z0-9]{4,20}$/.test(v.trim())) return "学号格式不正确 (4-20位字母或数字)";
        return false;
    };
    window.cwnuValidatePwd = function (v) {
        if (!v) return "密码不能为空";
        return false;
    };

    async function runAutoLogin() {
        const $ = window.jQuery || window.$;
        if (!$) {
            await alertBox("页面未就绪", "未检测到页面脚本 (jQuery)，请下拉刷新登录页后重试。\n当前: " + window.location.href);
            return;
        }

        log("登录页环境:",
            "needCaptcha=", (typeof window.needCaptcha !== "undefined" ? window.needCaptcha : "n/a"),
            "captchaSwitch=", (typeof window.captchaSwitch !== "undefined" ? window.captchaSwitch : "n/a"),
            "startLogin=", (typeof window.startLogin));

        const uid = await bridgePromise.showPrompt(
            "输入学号",
            "用于统一身份认证登录。\n凭据仅在本次页面登录中使用，不会被保存。",
            "", "cwnuValidateUid");
        if (uid === null) { toast("已取消"); return; }

        const pwd = await bridgePromise.showPrompt(
            "输入密码",
            "统一身份认证密码 (仅本次使用，不会保存)",
            "", "cwnuValidatePwd");
        if (pwd === null) { toast("已取消"); return; }

        const $user = $(".pwd_login #username");
        const $pwdInput = $(".pwd_login #password");
        log("表单元素:", "username=", $user.length, "password=", $pwdInput.length,
            "submit=", $("#login_submit").length);
        if (!$user.length || !$pwdInput.length) {
            await alertBox("未找到登录表单",
                "页面结构与预期不符，请直接在页面里手动输入学号密码登录。\n当前: " + window.location.href);
            return;
        }
        $user.val(uid.trim());
        $pwdInput.val(pwd);

        toast("正在提交登录...");
        try {
            if (typeof window.startLogin === "function") {
                window.startLogin(document.querySelector("#login_submit"));
            } else {
                $("#login_submit").click();
            }
        } catch (e) {
            log("触发登录失败:", e);
            await alertBox("自动提交失败", "请直接在页面中输入学号密码并点击登录按钮。\n错误: " + e.message);
            return;
        }

        await alertBox("登录已提交",
            "1. 若页面出现滑块/图片验证码，请手动完成 (账号密码已替你填好)\n" +
            "2. 登录成功后会自动尝试进入教务系统\n" +
            "3. 无论落在哪个页面，再点一次右上角\"执行导入\"：脚本会自动重试SSO或开始导入课表\n\n" +
            "若页面提示密码错误，请检查后重试 (多次失败会强制要求验证码)");
    }

    async function runEhallTransit() {
        log("处于信息门户:", window.location.href);

        try {
            const links = document.querySelectorAll("a");
            let found = 0;
            for (const a of links) {
                const t = (a.textContent || "").trim();
                const alt = (a.getAttribute("alt") || a.getAttribute("title") || "");
                if ((t.indexOf("教务") !== -1 || alt.indexOf("教务") !== -1) && t.length <= 30) {
                    log("门户链接遥测:", t || alt, "href=", a.href || a.getAttribute("href"),
                        "onclick=", a.getAttribute("onclick"));
                    found++;
                }
            }
            log("门户链接遥测完成, 命中", found, "条; 页面链接总数:", links.length);
        } catch (e) { log("链接遥测失败:", e); }

        let clicked = false;
        try {
            const candidates = document.querySelectorAll("a, li, div, span, p, img");
            for (const el of candidates) {
                let t = (el.textContent || "").trim();
                if (!t && el.getAttribute) {
                    t = el.getAttribute("alt") || el.getAttribute("title") || "";
                }
                if (t.length <= 12 && t.length >= 2 && t.indexOf("教务") !== -1 && el.offsetParent !== null) {
                    log("自动点击门户应用:", t, "tag=", el.tagName, "href=", el.href || "");
                    el.click();
                    clicked = true;
                    break;
                }
            }
        } catch (e) { log("门户自动点击失败:", e); }

        if (clicked) {
            toast("已点击教务系统入口, 等待跳转... 到达教务系统后请再点\"执行导入\"");
        } else {
            await alertBox("请手动进入教务系统",
                "未能自动找到\"教务系统\"应用图标。\n" +
                "请在门户页面中手动点击\"教务系统\"(或类似名称)应用,\n" +
                "进入教务系统后再点右上角\"执行导入\"。");
        }
    }

    function parseSemestersFromHtml(htmlText) {
        try {
            const doc = new DOMParser().parseFromString(htmlText, "text/html");
            const select = doc.getElementById("allSemesters");
            if (!select) return null;
            const options = Array.from(select.querySelectorAll("option")).map(function (opt) {
                return {
                    label: opt.textContent.trim(),
                    value: opt.value.trim(),
                    selected: opt.hasAttribute("selected") || opt.selected
                };
            }).filter(function (o) { return o.value && o.label; });
            return options.length > 0 ? options : null;
        } catch (e) {
            log("解析学期列表 HTML 失败:", e);
            return null;
        }
    }

    async function fetchSemesters() {
        try {
            const dom = parseSemestersFromHtml(document.documentElement.outerHTML);
            if (dom) { log("学期列表来自当前页面 DOM:", dom); return dom; }
        } catch (e) { log("当前页面 DOM 解析失败:", e); }

        try {
            const res = await fetch("/student/for-std/course-table", {
                headers: {
                    "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                    "x-requested-with": "XMLHttpRequest"
                },
                method: "GET",
                credentials: "include"
            });
            log("course-table 响应:", res.status, res.url);
            if (!res.ok) return null;
            const text = await res.text();
            if (text.indexOf("authserver") !== -1 || text.indexOf("/cas/login") !== -1) {
                log("警告: 响应疑似统一身份认证登录页, 登录态可能已失效");
            }
            return parseSemestersFromHtml(text);
        } catch (e) {
            log("course-table 请求失败:", e);
            return null;
        }
    }

    async function fetchSemesterMetadata(semesterId) {
        try {
            const res = await fetch("/student/ws/semester/get/" + semesterId, {
                headers: { "accept": "*/*", "x-requested-with": "XMLHttpRequest" },
                method: "GET",
                credentials: "include"
            });
            if (!res.ok) { log("semester/get 响应异常:", res.status); return null; }
            const data = await res.json();
            log("学期元数据:", JSON.stringify(data));
            return { startDate: data.startDate || null, endDate: data.endDate || null };
        } catch (e) {
            log("semester/get 失败 (不影响课程导入):", e);
            return null;
        }
    }

    async function probeGet(url, label) {
        try {
            const res = await fetch(url, {
                headers: { "accept": "*/*", "x-requested-with": "XMLHttpRequest" },
                method: "GET",
                credentials: "include"
            });
            const text = await res.text();
            log(label, "->", res.status, "len=", text.length, "head=", text.slice(0, 100).replace(/\s+/g, " "));
            if (!res.ok) return null;
            try { return JSON.parse(text); } catch (e) { log(label, "响应不是JSON"); return null; }
        } catch (e) {
            log(label, "请求失败:", e && e.message ? e.message : e);
            return null;
        }
    }

    function discoverIds() {
        const found = {};
        try {
            const names = ["dataId", "personId", "studentIds", "bizTypeId", "currentSemester", "semesters", "timetables"];
            for (const n of names) {
                const v = window[n];
                if (v !== undefined && v !== null) {
                    found[n] = (typeof v === "object") ? JSON.stringify(v).slice(0, 120) : String(v);
                }
            }
        } catch (e) { log("全局变量扫描异常:", e); }
        try {
            const html = document.documentElement.outerHTML;
            const m1 = html.match(/var\s+dataId\s*=\s*"?(\d+)"?/); if (m1) found.htmlDataId = m1[1];
            const m2 = html.match(/var\s+personId\s*=\s*"?(\d+)"?/); if (m2) found.htmlPersonId = m2[1];
            const m3 = html.match(/var\s+bizTypeId\s*=\s*"?(\d+)"?/); if (m3) found.htmlBizTypeId = m3[1];
        } catch (e) { log("HTML变量扫描异常:", e); }
        log("ID/参数发现结果:", JSON.stringify(found));
        return found;
    }

    function parseActivities(rawActivities) {
        const parsedCourses = [];
        let sampleLogged = 0;
        for (const act of rawActivities) {
            if (!act.courseName || !act.weekday || !act.startUnit || !act.endUnit ||
                !Array.isArray(act.weekIndexes)) {
                log("跳过不完整的 activity:", JSON.stringify(act).slice(0, 300));
                continue;
            }
            if (sampleLogged < 2) {
                sampleLogged++;
                log("activity[" + (sampleLogged-1) + "] 完整结构:", JSON.stringify(act).slice(0, 600));
            }
            let teacherName = "";
            if (Array.isArray(act.teachers) && act.teachers.length > 0) {
                teacherName = act.teachers.map(function (t) {
                    return String(t).replace(/\(\d+\)/g, "").replace(/\[\d+\]/g, "").trim();
                }).filter(Boolean).join(",");
            } else if (typeof act.teachers === "string") {
                teacherName = act.teachers.replace(/\(\d+\)/g, "").trim();
            }
            const weeks = act.weekIndexes.map(Number)
                .filter(function (w) { return Number.isInteger(w) && w > 0; })
                .sort(function (a, b) { return a - b; });
            if (weeks.length === 0) continue;

            parsedCourses.push({
                name: String(act.courseName).trim(),
                teacher: teacherName || "未知教师",
                position: String(act.room || act.building || "未知地点").trim(),
                day: Number(act.weekday),
                startSection: Number(act.startUnit),
                endSection: Number(act.endUnit),
                weeks: weeks
            });
        }
        const map = new Map();
        for (const c of parsedCourses) {
            if (!c.weeks.length) continue;
            const key = JSON.stringify([c.name, c.teacher, c.position, c.day, c.startSection, c.endSection]);
            if (map.has(key)) {
                const ex = map.get(key);
                ex.weeks = Array.from(new Set(ex.weeks.concat(c.weeks))).sort(function (a, b) { return a - b; });
            } else {
                map.set(key, c);
            }
        }
        return Array.from(map.values());
    }

    const DAY_CHAR_MAP = { "一": 1, "二": 2, "三": 3, "四": 4, "五": 5, "六": 6, "日": 7, "天": 7 };

    function cnNum(s) {
        s = String(s).trim();
        if (/^\d+$/.test(s)) return parseInt(s, 10);
        const d = { "零": 0, "一": 1, "二": 2, "三": 3, "四": 4, "五": 5, "六": 6, "七": 7, "八": 8, "九": 9 };
        if (s === "十") return 10;
        const i = s.indexOf("十");
        if (i === -1) return d[s] !== undefined ? d[s] : NaN;
        const tens = i === 0 ? 1 : (d[s.slice(0, i)] || 0);
        const rest = s.slice(i + 1);
        const ones = rest === "" ? 0 : (d[rest] || 0);
        return tens * 10 + ones;
    }

    function parseWeekList(s) {
        const weeks = [];
        const parts = String(s).split(/[,，、]/);
        for (const part of parts) {
            const seg = part.split(/[~\-—]/);
            if (seg.length >= 2) {
                const a = parseInt(seg[0], 10), b = parseInt(seg[1], 10);
                if (!isNaN(a) && !isNaN(b)) for (let i = a; i <= b; i++) weeks.push(i);
            } else {
                const a = parseInt(seg[0], 10);
                if (!isNaN(a)) weeks.push(a);
            }
        }
        return weeks.filter(function (w) { return w > 0; });
    }

    function extractTeacher(lesson) {
        function clean(s) { return String(s).replace(/\(\d+\)/g, "").replace(/\[\d+\]/g, "").trim(); }
        if (lesson.teacherAssignmentString) return clean(lesson.teacherAssignmentString) || "未知教师";
        if (lesson.teacherAssignmentStr) return clean(lesson.teacherAssignmentStr) || "未知教师";
        const list = lesson.teacherAssignmentList;
        if (Array.isArray(list) && list.length > 0) {
            const names = list.map(function (t) {
                if (!t) return "";
                if (typeof t === "string") return t;
                if (t.teacher && (t.teacher.nameZh || t.teacher.name)) return t.teacher.nameZh || t.teacher.name;
                return t.nameZh || t.name || t.teacherName || "";
            }).map(clean).filter(Boolean);
            if (names.length > 0) return names.join(",");
        }
        return "未知教师";
    }

    function parseLessons(lessons, skipIds) {
        const courses = [];
        let sampleLogged = 0;
        for (const lesson of lessons) {
            try {
                if (skipIds && skipIds.indexOf(lesson.id) !== -1) { log("跳过 notAttend 课程:", lesson.id); continue; }
                if (sampleLogged < 2) {
                    sampleLogged++;
                    log("lesson[" + (sampleLogged-1) + "] 完整结构:", JSON.stringify(lesson).slice(0, 800));
                    log("lesson[" + (sampleLogged-1) + "] 字段名:", Object.keys(lesson).join(","));
                    if (lesson.course) log("lesson[" + (sampleLogged-1) + "].course:", JSON.stringify(lesson.course).slice(0, 400));
                }
                const st = lesson.scheduleText;
                if (!st) { log("lesson 无 scheduleText:", JSON.stringify(lesson).slice(0, 250)); continue; }
                let raw = "";
                if (st.dateTimePlaceText && st.dateTimePlaceText.text) raw = st.dateTimePlaceText.text;
                else if (st.dateTimePlacePersonText && st.dateTimePlacePersonText.text) raw = st.dateTimePlacePersonText.text;
                else if (st.dateTimeText && st.dateTimeText.text) raw = st.dateTimeText.text;
                if (!raw) { log("scheduleText 无 text 字段:", JSON.stringify(st).slice(0, 250)); continue; }
                const name = (lesson.course && (lesson.course.nameZh || lesson.course.name)) || lesson.nameZh || "未知课程";
                const teacher = extractTeacher(lesson);
                const lines = raw.split(/;\s*\n\s*/).filter(function (l) { return l.trim(); });
                for (const line of lines) {
                    let m = line.trim().match(/^(\d+(?:\s*[~\-—]\s*\d+)?(?:\s*[,，、]\s*\d+(?:\s*[~\-—]\s*\d+)?)*)\s*周\s*(?:周|星期)\s*([一二三四五六日天])\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节\s*(?:[~\-—至到]\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节)?\s*(.*)$/);
                    let weeks = null;
                    let day = null;
                    let startSec = null;
                    let endSec = null;
                    let position = (m && m[5]) ? m[5].trim() : "";
                    if (m) {
                        weeks = parseWeekList(m[1]);
                        day = DAY_CHAR_MAP[m[2]] || 1;
                        startSec = cnNum(m[3]);
                        endSec = m[4] ? cnNum(m[4]) : startSec;
                    } else {
                        const m2 = line.trim().match(/^(\d+(?:\s*[~\-—]\s*\d+)?(?:\s*[,，、]\s*\d+(?:\s*[~\-—]\s*\d+)?)*)\s*(?:\(\s*单\s*\)\s*周|周\s*\(\s*单\s*\))\s*(?:周|星期)\s*([一二三四五六日天])\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节\s*(?:[~\-—至到]\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节)?\s*(.*)$/);
                        if (m2) {
                            weeks = parseWeekList(m2[1]).filter(function (w) { return w % 2 === 1; });
                            day = DAY_CHAR_MAP[m2[2]] || 1;
                            startSec = cnNum(m2[3]);
                            endSec = m2[4] ? cnNum(m2[4]) : startSec;
                            position = (m2[5] || "").trim();
                            log("匹配到单周格式:", line, "weeks=", weeks);
                        } else {
                            const m3 = line.trim().match(/^(\d+(?:\s*[~\-—]\s*\d+)?(?:\s*[,，、]\s*\d+(?:\s*[~\-—]\s*\d+)?)*)\s*(?:\(\s*双\s*\)\s*周|周\s*\(\s*双\s*\))\s*(?:周|星期)\s*([一二三四五六日天])\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节\s*(?:[~\-—至到]\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节)?\s*(.*)$/);
                            if (m3) {
                                weeks = parseWeekList(m3[1]).filter(function (w) { return w % 2 === 0; });
                                day = DAY_CHAR_MAP[m3[2]] || 1;
                                startSec = cnNum(m3[3]);
                                endSec = m3[4] ? cnNum(m3[4]) : startSec;
                                position = (m3[5] || "").trim();
                                log("匹配到双周格式:", line, "weeks=", weeks);
                            } else {
                                const m4 = line.trim().match(/^单周\s*(?:周|星期)\s*([一二三四五六日天])\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节\s*(?:[~\-—至到]\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节)?\s*(.*)$/);
                                if (m4) {
                                    weeks = [1,3,5,7,9,11,13,15,17,19];
                                    day = DAY_CHAR_MAP[m4[1]] || 1;
                                    startSec = cnNum(m4[2]);
                                    endSec = m4[3] ? cnNum(m4[3]) : startSec;
                                    position = (m4[4] || "").trim();
                                    log("匹配到纯单周格式:", line);
                                } else {
                                    const m5 = line.trim().match(/^双周\s*(?:周|星期)\s*([一二三四五六日天])\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节\s*(?:[~\-—至到]\s*第?\s*([一二三四五六七八九十零]+|\d+)\s*节)?\s*(.*)$/);
                                    if (m5) {
                                        weeks = [2,4,6,8,10,12,14,16,18,20];
                                        day = DAY_CHAR_MAP[m5[1]] || 1;
                                        startSec = cnNum(m5[2]);
                                        endSec = m5[3] ? cnNum(m5[3]) : startSec;
                                        position = (m5[4] || "").trim();
                                        log("匹配到纯双周格式:", line);
                                    }
                                }
                            }
                        }
                    }
                    if (!weeks || !day || isNaN(startSec) || isNaN(endSec)) {
                        log("无法解析排课行:", line);
                        continue;
                    }
                    if (!position) {
                        position = (lesson.room || lesson.building || lesson.location || lesson.place || "");
                        position = String(position).trim();
                    }
                    if (!position) position = "未知地点";
                    courses.push({
                        name: String(name).trim(),
                        teacher: String(teacher).replace(/\(\d+\)/g, "").trim() || "未知教师",
                        position: position,
                        day: day,
                        startSection: startSec,
                        endSection: endSec,
                        weeks: weeks
                    });
                }
            } catch (e) { log("lesson 解析异常:", e && e.message ? e.message : e); }
        }
        const map = new Map();
        for (const c of courses) {
            if (!c.weeks.length) continue;
            const key = JSON.stringify([c.name, c.teacher, c.position, c.day, c.startSection, c.endSection]);
            if (map.has(key)) {
                const ex = map.get(key);
                ex.weeks = Array.from(new Set(ex.weeks.concat(c.weeks))).sort(function (a, b) { return a - b; });
            } else {
                map.set(key, c);
            }
        }
        return Array.from(map.values());
    }

    function parseAnyFormat(data) {
        const rawActivities =
            (data.studentTableVms && data.studentTableVms[0] ? data.studentTableVms[0].activities : null) ||
            (data.studentTableVm ? data.studentTableVm.activities : null) ||
            data.activities || null;
        if (Array.isArray(rawActivities) && rawActivities.length > 0) {
            log("activities 条数:", rawActivities.length);
            const courses = parseActivities(rawActivities);
            log("activities 解析成功条数:", courses.length);
            if (courses.length > 0) return courses;
        }
        if (Array.isArray(data.lessons) && data.lessons.length > 0) {
            log("lessons 条数:", data.lessons.length, "第一条样本:", JSON.stringify(data.lessons[0]).slice(0, 400));
            const courses = parseLessons(data.lessons, data.notAttendLessonIds || null);
            log("lessons 解析成功条数:", courses.length);
            if (courses.length > 0) return courses;
        }
        return null;
    }

    function installNetHook() {
        if (window.__cwnuHooked) { log("网络钩子已存在, 跳过安装"); return; }
        window.__cwnuHooked = true;
        window.__cwnuCap = [];
        try {
            const of = window.fetch;
            window.fetch = async function (input, init) {
                const url = (typeof input === "string") ? input : (input && input.url) || String(input);
                const method = (init && init.method) || (input && input.method) || "GET";
                const body = (init && init.body) ? String(init.body).slice(0, 300) : "";
                try {
                    const res = await of.apply(this, arguments);
                    try {
                        const text = await res.clone().text();
                        window.__cwnuCap.push({ kind: "fetch", url: String(url).slice(0, 300), method: method, body: body, status: res.status, len: text.length, head: text.slice(0, 400) });
                        log("[hook]fetch", method, url, "->", res.status, "len=", text.length);
                    } catch (e) { /* ignore */ }
                    return res;
                } catch (e) {
                    window.__cwnuCap.push({ kind: "fetch-error", url: String(url).slice(0, 300), method: method, body: body, status: 0, len: 0, head: String(e && e.message) });
                    throw e;
                }
            };
        } catch (e) { log("fetch 钩子安装失败:", e); }
        try {
            const oo = XMLHttpRequest.prototype.open;
            const os = XMLHttpRequest.prototype.send;
            XMLHttpRequest.prototype.open = function (m, u) { this.__cwnuM = m; this.__cwnuU = u; return oo.apply(this, arguments); };
            XMLHttpRequest.prototype.send = function (b) {
                const xhr = this;
                xhr.addEventListener("loadend", function () {
                    try {
                        let head = "";
                        try { head = String(xhr.responseText || "").slice(0, 400); } catch (e) { /* ignore */ }
                        window.__cwnuCap.push({ kind: "xhr", url: String(xhr.__cwnuU).slice(0, 300), method: xhr.__cwnuM, body: b ? String(b).slice(0, 300) : "", status: xhr.status, len: head.length, head: head });
                        log("[hook]xhr", xhr.__cwnuM, xhr.__cwnuU, "->", xhr.status);
                    } catch (e) { /* ignore */ }
                });
                return os.apply(this, arguments);
            };
        } catch (e) { log("XHR 钩子安装失败:", e); }
        log("网络钩子安装完成");
    }

    function enumeratePageVars() {
        try {
            const html = document.documentElement.outerHTML;
            const names = [];
            const re = /var\s+([A-Za-z_$][\w$]*)\s*=/g;
            let m;
            while ((m = re.exec(html)) !== null) { if (names.indexOf(m[1]) === -1) names.push(m[1]); }
            log("页面内联var变量(" + names.length + "):", names.join(","));
            for (const n of names) {
                if (!/student|person|user|data|account|login|uid|sid/i.test(n)) continue;
                try {
                    const v = window[n];
                    if (v !== undefined && v !== null) {
                        log("可疑变量值:", n, "=", (typeof v === "object" ? JSON.stringify(v) : String(v)).slice(0, 200));
                    }
                } catch (e) { /* ignore */ }
            }
        } catch (e) { log("var枚举失败:", e); }
    }

    async function triggerPageReload(semesterId) {
        try {
            const $ = window.jQuery;
            if ($ && $("#allSemesters").length) {
                $("#allSemesters").val(String(semesterId)).trigger("change");
                log("已用 jQuery 触发 #allSemesters change");
            } else {
                const sel = document.getElementById("allSemesters");
                if (sel) {
                    sel.value = String(semesterId);
                    sel.dispatchEvent(new Event("change", { bubbles: true }));
                    log("已用原生 Event 触发 change");
                } else {
                    log("未找到 #allSemesters, 无法触发页面刷新");
                }
            }
        } catch (e) { log("触发页面刷新失败:", e); }
        await new Promise(function (r) { setTimeout(r, 4500); });
    }

    async function replayCapture(c) {
        try {
            log("重放捕获请求:", c.method, c.url, "body=", c.body);
            const opt = {
                credentials: "include",
                method: (c.method || "GET").toUpperCase(),
                headers: { "x-requested-with": "XMLHttpRequest", "accept": "*/*" }
            };
            if (c.body && opt.method === "POST") {
                opt.body = c.body;
                opt.headers["content-type"] = "application/x-www-form-urlencoded; charset=UTF-8";
            }
            const res = await fetch(c.url, opt);
            const text = await res.text();
            log("重放响应:", res.status, "len=", text.length, "head=", text.slice(0, 200).replace(/\s+/g, " "));
            if (!res.ok) return null;
            return JSON.parse(text);
        } catch (e) {
            log("重放失败:", e && e.message ? e.message : e);
            return null;
        }
    }

    async function fetchAndParseCourses(semesterId) {
        const ids = discoverIds();
        let studentId = ids.dataId || ids.htmlDataId || ids.personId || ids.htmlPersonId || null;
        try {
            if (!studentId && window.studentIds && window.studentIds.length) studentId = String(window.studentIds[0]);
        } catch (e) { /* ignore */ }
        log("使用的 studentId/dataId:", studentId);

        let data = null;
        let used = "";

        if (studentId) {
            data = await probeGet("/student/for-std/course-table/semester/" + semesterId + "/print-data/" + studentId, "端点A(print-data/学号)");
            if (data) used = "A";
        }
        if (!data && studentId) {
            const biz = ids.bizTypeId || ids.htmlBizTypeId || "2";
            data = await probeGet("/student/for-std/course-table/get-data?bizTypeId=" + biz + "&semesterId=" + semesterId + "&dataId=" + studentId, "端点B(get-data)");
            if (data) used = "B";
        }
        if (!data) {
            const biz = ids.bizTypeId || ids.htmlBizTypeId || "2";
            data = await probeGet("/student/for-std/course-table/get-data?bizTypeId=" + biz + "&semesterId=" + semesterId, "端点B2(get-data无dataId)");
            if (data) used = "B2";
        }
        if (!data) {
            data = await probeGet("/student/for-std/course-table/semester/" + semesterId + "/print-data", "端点C(print-data裸)");
            if (data) used = "C";
        }
        if (!data) {
            data = await probeGet("/student/for-std/course-table/semester/" + semesterId + "/print-data?semesterId=" + semesterId + "&hasExperiment=true", "端点D(print-data原版)");
            if (data) used = "D";
        }

        if (!data) {
            log("静态端点均失败, 启动动态捕获");
            installNetHook();
            enumeratePageVars();
            window.__cwnuCap.length = 0;
            await triggerPageReload(semesterId);
            const caps = window.__cwnuCap.slice();
            window.__cwnuCap.length = 0;
            log("捕获请求数:", caps.length);
            for (const c of caps) log("捕获条目:", JSON.stringify(c).slice(0, 550));
            for (let i = caps.length - 1; i >= 0; i--) {
                const c = caps[i];
                if (c.status !== 200) continue;
                if (!/course|table|data|schedule|timetable|print|lesson/i.test(c.url)) continue;
                const replay = await replayCapture(c);
                if (!replay) continue;
                log("重放数据顶层键:", Object.keys(replay).join(","));
                const parsed = parseAnyFormat(replay);
                if (parsed && parsed.length > 0) {
                    log("动态捕获接口解析成功:", c.url, "条数:", parsed.length);
                    return parsed;
                }
            }
            try {
                const tables = document.querySelectorAll("table");
                log("DOM兜底遥测: table数量=", tables.length);
                for (let i = 0; i < Math.min(tables.length, 5); i++) {
                    log("table", i, "class=", tables[i].className,
                        "片段=", tables[i].innerHTML.slice(0, 200).replace(/\s+/g, " "));
                }
                const text = document.body.innerText || "";
                log("body文本长度=", text.length);
                const idx = text.search(/星期[一二三四五六日]/);
                if (idx !== -1) log("body文本星期片段:", text.slice(Math.max(0, idx - 100), idx + 300).replace(/\s+/g, " "));
            } catch (e) { /* ignore */ }
            return null;
        }

        log("采用端点:", used, "顶层键:", Object.keys(data).join(","));
        diagLog("原始API响应(端点" + used + ")", JSON.stringify(data).slice(0, 60000));
        const courses = parseAnyFormat(data);
        if (courses && courses.length > 0) {
            diagLog("解析后课程(" + courses.length + "条)", courses);
            return courses;
        }
        log("数据格式未识别, 响应片段:", JSON.stringify(data).slice(0, 600));
        return null;
    }

    function calculateTotalWeeks(startDate, endDate) {
        if (!startDate || !endDate) return 20;
        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffMs = end.getTime() - start.getTime();
        if (isNaN(diffMs) || diffMs <= 0) return 20;
        const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));
        return Math.min(Math.max(Math.ceil(diffDays / 7), 16), 30);
    }

    async function runImportFlow() {
        if (window.location.pathname.indexOf("/student/login") !== -1) {
            const bounced = window.sessionStorage.getItem("cwnu_via_portal");
            if (!bounced) {
                window.sessionStorage.setItem("cwnu_via_portal", "1");
                log("检测到教务本地登录页, 返回信息门户走正规SSO入口");
                toast("正在返回信息门户, 请点击\"教务系统\"应用...");
                window.location.href = "https://ehall.cwnu.edu.cn/index.html";
                return;
            }
            await alertBox("请通过信息门户进入教务系统",
                "本校教务系统不支持直接登录, 必须从信息门户进入。\n" +
                "操作: 在门户中点击\"教务系统\"应用, 进入后再点右上角\"执行导入\"。\n\n" +
                "当前页面: " + window.location.href);
            return;
        }

        const confirmed = await bridgePromise.showAlert(
            "西华师范大学课表导入",
            "即将自动获取学期列表并导入课程、学期配置与西华作息时间。\n(若尚未登录，请先返回统一认证页面用\"执行导入\"自动登录)",
            "开始导入");
        if (!confirmed) { toast("已取消导入"); return; }

        toast("正在获取学期列表...");
        const semesters = await fetchSemesters();
        if (!semesters || semesters.length === 0) {
            await alertBox("获取学期列表失败",
                "未能从教务系统获取学期列表。请检查:\n" +
                "1. 是否已成功登录 (若跳转到了统一认证登录页, 请先完成登录)\n" +
                "2. 当前是否在教务系统课表页面（URL 包含 /student/for-std/course-table）\n" +
                "3. 可尝试先在教务系统中打开一次\"课表\"页面再执行导入\n\n" +
                "当前页面: " + window.location.href);
            return;
        }

        const labels = semesters.map(function (s) { return s.label; });
        let defaultIndex = semesters.findIndex(function (s) { return s.selected; });
        if (defaultIndex < 0) defaultIndex = 0;

        const selectedIndex = await bridgePromise.showSingleSelection(
            "选择学期", JSON.stringify(labels), defaultIndex);
        if (selectedIndex === null || selectedIndex < 0 || selectedIndex >= semesters.length) {
            toast("已取消导入");
            return;
        }

        const selectedSemester = semesters[selectedIndex];
        log("已选择学期:", JSON.stringify(selectedSemester));
        toast("正在拉取课表数据...");

        const results = await Promise.all([
            fetchSemesterMetadata(selectedSemester.value),
            fetchAndParseCourses(selectedSemester.value)
        ]);
        const meta = results[0];
        const courses = results[1];

        diagLog("所选学期与元数据", { selected: selectedSemester, meta: meta });

        if (!courses || courses.length === 0) {
            await alertBox("未查询到课程数据 (" + SCRIPT_VERSION + ")",
                "该学期没有解析到有效课程。可能原因:\n" +
                "1. 所选学期尚未发布课表\n" +
                "2. 教务系统接口结构与预期不同");
            return;
        }

        let totalWeeks = 20;
        if (meta && meta.startDate && meta.endDate) {
            totalWeeks = calculateTotalWeeks(meta.startDate, meta.endDate);
        } else {
            const allWeeks = courses.flatMap(function (c) { return c.weeks || []; });
            if (allWeeks.length > 0) totalWeeks = Math.max.apply(null, allWeeks);
        }

        const validStartDate = (meta && meta.startDate && /^\d{4}-\d{2}-\d{2}$/.test(meta.startDate))
            ? meta.startDate : null;
        const configData = {
            semesterStartDate: validStartDate,
            semesterTotalWeeks: Math.max(totalWeeks, 16),
            firstDayOfWeek: 1,
            defaultClassDuration: 40,
            defaultBreakDuration: 10
        };
        log("课表配置:", JSON.stringify(configData));

        try {
            await bridgePromise.saveCourseConfig(JSON.stringify(configData));
            log("配置保存成功");
        } catch (e) { log("配置保存失败 (继续导入课程):", e); }

        try {
            await bridgePromise.savePresetTimeSlots(JSON.stringify(presetTimeSlots));
            log("作息时间保存成功");
        } catch (e) { log("作息时间保存失败 (继续导入课程):", e); }

        try {
            const ok = await bridgePromise.saveImportedCourses(JSON.stringify(courses));
            if (!ok) { toast("课程保存失败"); return; }
        } catch (e) {
            log("课程保存失败:", e);
            await alertBox("课程保存失败", String(e && e.message ? e.message : e));
            return;
        }

        toast("导入成功! 共 " + courses.length + " 门课程, 已同步西华作息时间");
        log("导入流程全部完成");

        if (bridge && bridge.notifyTaskCompletion) bridge.notifyTaskCompletion();
    }

    try {
        const host = window.location.host || "";
        const path = window.location.pathname || "";
        const href = window.location.href || "";
        log("脚本启动 " + SCRIPT_VERSION + ", 当前页面:", href);
        diagLog("脚本启动环境", {
            scriptVersion: SCRIPT_VERSION,
            host: host,
            path: path,
            href: href,
            userAgent: navigator.userAgent,
            hasJQuery: !!(window.jQuery || window.$),
            hasBridge: !!bridge,
            hasBridgePromise: !!bridgePromise
        });
        if (host.indexOf("authserver.") !== -1) {
            await runAutoLogin();
        } else if (host.indexOf("ehall.") !== -1 || href.indexOf("ehall.") !== -1) {
            await runEhallTransit();
        } else if (path.indexOf("/student/for-std/course-table") !== -1) {
            await runImportFlow();
        } else if (host.indexOf("jwxt.") !== -1 || href.indexOf("jwxt.") !== -1) {
            await runImportFlow();
        } else {
            await alertBox("页面不匹配",
                "请在统一认证 / 信息门户 / 教务系统页面执行导入。\n当前页面: " + href);
        }
    } catch (e) {
        log("执行流程发生未捕获异常:", e);
        diagLog("未捕获异常", String(e && e.stack ? e.stack : e));
        toast("执行失败: " + (e && e.message ? e.message : e));
        await alertBox("执行流程异常", String(e && e.stack ? e.stack : e));
    }
})();
