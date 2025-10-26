import defaultImg from "@/assets/default.jpg";

export default {
  mounted(el, binding) {
    if (!binding.value) {
      el.src = defaultImg;
    } else {
      el.src = binding.value;
    }

    el.onerror = () => {
      el.src = defaultImg; 
    };
  },
  updated(el, binding) {
    if (!binding.value) {
      el.src = defaultImg;
    } else {
      el.src = binding.value;
    }
  }
};