/* KyloAPI — site script: dock-nagyítás, reveal, másolás, aktív pill */

// 1) macOS dock-nagyítás: az egér távolsága szerint skálázza az íkono­kat
const dock = document.getElementById("dock");
if (dock) {
  const icons = [...dock.querySelectorAll(".dico")];
  const baseScale = 1;
  const maxScale = 1.5;
  const range = 110;

  dock.addEventListener("mousemove", (e) => {
    icons.forEach((icon) => {
      const rect = icon.getBoundingClientRect();
      const distance = Math.abs(e.clientX - (rect.left + rect.width / 2));
      const t = Math.max(0, 1 - distance / range);
      const scale = baseScale + (maxScale - baseScale) * (t * t);
      icon.style.transform = `scale(${scale}) translateY(${-6 * t}px)`;
    });
  });

  dock.addEventListener("mouseleave", () => {
    icons.forEach((icon) => {
      icon.style.transform = "";
    });
  });
}

// 2) Beolvasás-animáció (IntersectionObserver)
const observer = new IntersectionObserver((entries) => {
  entries.forEach((entry) => {
    if (entry.isIntersecting) {
      entry.target.classList.add("on");
      observer.unobserve(entry.target);
    }
  });
}, { threshold: 0.12 });

document.querySelectorAll(".reveal").forEach((el) => observer.observe(el));

// 3) Másolás gombok
document.querySelectorAll(".copy").forEach((button) => {
  button.addEventListener("click", () => {
    const box = button.closest(".codebox");
    const pre = box ? box.querySelector("pre") : null;
    if (!pre) return;
    navigator.clipboard.writeText(pre.innerText).then(() => {
      const old = button.textContent;
      button.textContent = "✓ kimásolva";
      setTimeout(() => { button.textContent = old; }, 1400);
    });
  });
});

// 4) Aktív nav-pill scroll szerint
const pills = [...document.querySelectorAll(".nav a.pill")];
const targets = pills
  .filter((p) => p.getAttribute("href").startsWith("#"))
  .map((p) => document.querySelector(p.getAttribute("href")))
  .filter(Boolean);

const spy = new IntersectionObserver((entries) => {
  entries.forEach((entry) => {
    if (entry.isIntersecting) {
      pills.forEach((p) => p.classList.remove("active"));
      const active = pills.find(
        (p) => p.getAttribute("href") === "#" + entry.target.id
      );
      if (active) active.classList.add("active");
    }
  });
}, { rootMargin: "-40% 0px -55% 0px" });

targets.forEach((t) => spy.observe(t));
