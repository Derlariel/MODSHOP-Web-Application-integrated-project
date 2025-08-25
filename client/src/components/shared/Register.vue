<script setup>
import { reactive, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/useAuthStore";
import BaseInput from "@/components/shared/BaseInput.vue";
import {
  runValidation,
  validateMinLength,
  validateMaxLength,
  validateNoWhitespace,
} from "@/utils/validate";

const router = useRouter();
const auth = useAuthStore();

const form = reactive({
  accountType: "BUYER", // BUYER | SELLER
  nickname: "",
  email: "",
  password: "",
  fullName: "",
  // ฟิลด์ของ Seller เท่านั้น
  mobile: "",
  bankAccountNo: "",
  bankName: "",
  nationalIdNumber: "",
  nationalIdFront: null, // File
  nationalIdBack: null, // File
});

const errors = reactive({});
const isSeller = computed(() => form.accountType === "SELLER");

const vEmail = (data) => {
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(data || "").trim());
  return { valid: ok, message: ok ? null : "Email is invalid." };
};

const vPasswordPolicy = (data) => {
  const s = String(data || "");
  const ok =
    s.length >= 8 &&
    /[a-z]/.test(s) &&
    /[A-Z]/.test(s) &&
    /[0-9]/.test(s) &&
    /[^A-Za-z0-9]/.test(s);
  return {
    valid: ok,
    message: ok
      ? null
      : "Password must be at least 8 chars and include lower, upper, number, and special character.",
  };
};

const vRequired = (label) => (data) => {
  const ok = String(data ?? "").trim().length > 0;
  return { valid: ok, message: ok ? null : `${label} is required.` };
};

const vFullNameLen = (data) => {
  const len = String(data || "").trim().length;
  const ok = len >= 4 && len <= 40;
  return {
    valid: ok,
    message: ok ? null : "Full name must be 4–40 characters.",
  };
};

const vThaiMobile = (data) => {
  const ok = /^0\d{9}$/.test(String(data || "").trim());
  return {
    valid: ok,
    message: ok ? null : "Mobile number must be 10 digits (Thai).",
  };
};

const vNationalId13 = (data) => {
  const ok = /^\d{13}$/.test(String(data || "").trim());
  return { valid: ok, message: ok ? null : "National ID must be 13 digits." };
};

const vFileRequired = (label) => (file) => {
  const ok = !!file;
  return { valid: ok, message: ok ? null : `${label} is required.` };
};

const vFileImageMax = (maxMB) => (file) => {
  if (!file) return { valid: true, message: null };
  const typeOK = file.type && file.type.startsWith("image/");
  const sizeOK = file.size <= maxMB * 1024 * 1024;
  if (!typeOK) return { valid: false, message: "File must be an image." };
  if (!sizeOK) return { valid: false, message: `File must be ≤ ${maxMB} MB.` };
  return { valid: true, message: null };
};

const rules = {
  // ทั้ง Buyer & Seller
  nickname: [vRequired("Nickname"), validateNoWhitespace],
  email: [vRequired("Email"), vEmail],
  password: [vRequired("Password"), vPasswordPolicy],
  fullName: [vRequired("Full name"), vFullNameLen],

  // เฉพาะ Seller
  mobile: [vRequired("Mobile number"), vThaiMobile],
  bankAccountNo: [
    vRequired("Bank account number"),
    validateMinLength(3),
    validateMaxLength(30),
  ],
  bankName: [
    vRequired("Bank name"),
    validateMinLength(2),
    validateMaxLength(60),
  ],
  nationalIdNumber: [vRequired("National ID number"), vNationalId13],
  nationalIdFront: [vFileRequired("National ID (front)"), vFileImageMax(5)],
  nationalIdBack: [vFileRequired("National ID (back)"), vFileImageMax(5)],
};

function validateField(name) {
  errors[name] = null;

  const sellerOnly = [
    "mobile",
    "bankAccountNo",
    "bankName",
    "nationalIdNumber",
    "nationalIdFront",
    "nationalIdBack",
  ];
  if (!isSeller.value && sellerOnly.includes(name)) return true;

  const fns = rules[name];
  if (!fns) return true;

  const { valid, message } = runValidation(form[name], fns);
  errors[name] = message;
  return valid;
}

function validateAll() {
  const fields = ["nickname", "email", "password", "fullName"];
  if (isSeller.value) {
    fields.push(
      "mobile",
      "bankAccountNo",
      "bankName",
      "nationalIdNumber",
      "nationalIdFront",
      "nationalIdBack"
    );
  }
  let ok = true;
  fields.forEach((f) => {
    if (!validateField(f)) ok = false;
  });
  return ok;
}

const submitDisabled = computed(() => {
  if (auth.isSubmitting) return true;
  return !validateAll();
});

function onFileChange(e, key) {
  const file =
    e.target && e.target.files && e.target.files[0] ? e.target.files[0] : null;
  form[key] = file;
  validateField(key);
}

async function onSubmit() {
  if (!validateAll()) return;

  const fd = new FormData();
  fd.append("role", form.accountType);
  fd.append("nickname", form.nickname.trim());
  fd.append("email", form.email.trim());
  fd.append("password", form.password);
  fd.append("fullname", form.fullName.trim());

  if (isSeller.value) {
    fd.append("mobileNumber", form.mobile.trim());
    fd.append("bankAccountNumber", form.bankAccountNo.trim());
    fd.append("bankName", form.bankName.trim());
    fd.append("nationalIdNumber", form.nationalIdNumber.trim());
    fd.append("nationalIdPhotoFront", form.nationalIdFront);
    fd.append("nationalIdPhotoBack", form.nationalIdBack);
  }

  console.log(fd)

  try {
    await auth.register(fd); 
    sessionStorage.setItem("register-success", "The user account has been successfully registered.");
    router.push("/sale-items");
  } catch (e) {
    alert(e?.message || "Registration failed!");
  }
}
</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div class="max-w-xl mx-auto px-6 pt-24 pb-16">
      <h1 class="text-3xl font-semibold mb-8">Register</h1>
      <!-- Account Type -->
      <div class="mb-6">
        <label class="block mb-2 text-sm text-gray-300">Account Type</label>
        <div class="flex gap-4 account-type">
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              type="radio"
              value="BUYER"
              v-model="form.accountType"
              class="accent-white"
              @change="
                () => {
                  [
                    'mobile',
                    'bankAccountNo',
                    'bankName',
                    'nationalIdNumber',
                    'nationalIdFront',
                    'nationalIdBack',
                  ].forEach((k) => (errors[k] = null));
                }
              "
            />
            <span>Buyer</span>
          </label>
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              type="radio"
              value="SELLER"
              v-model="form.accountType"
              class="accent-white"
              @change="() => {}"
            />
            <span>Seller</span>
          </label>
        </div>
      </div>

      <!-- Common fields -->
      <div class="space-y-5">
        <BaseInput
          v-model="form.nickname"
          label="Nickname"
          placeholder="nick"
          required
          cypress="nickname"
          :error="errors.nickname"
          @trim="validateField('nickname')"
        />

        <BaseInput
          v-model="form.email"
          label="Email"
          type="email"
          placeholder="you@example.com"
          required
          cypress="email"
          :error="errors.email"
          @trim="validateField('email')"
        />

        <div class="space-y-1">
          <BaseInput
            v-model="form.password"
            label="Password"
            type="password"
            placeholder="••••••••"
            required
            cypress="password"
            :error="errors.password"
            @trim="validateField('password')"
          />
          <p class="text-xs text-gray-400 mt-1">
            min 8 chars, include lower, upper, number, special char
          </p>
        </div>

        <BaseInput
          v-model="form.fullName"
          label="Full name"
          placeholder="John Doe"
          required
          cypress="fullname"
          :error="errors.fullName"
          :maxInput="40"
          @trim="validateField('fullName')"
        />
      </div>

      <!-- Seller fields -->
      <div
        v-if="isSeller"
        class="space-y-5 mt-8 border-t border-neutral-800 pt-8"
      >
        <h3 class="text-lg font-medium">Seller Information</h3>

        <BaseInput
          v-model="form.mobile"
          label="Mobile number"
          placeholder="08XXXXXXXX"
          required
          cypress="mobile"
          :error="errors.mobile"
          @trim="validateField('mobile')"
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <BaseInput
            v-model="form.bankAccountNo"
            label="Bank account no."
            placeholder="123-4-56789-0"
            required
            cypress="bank-account-no"
            :error="errors.bankAccountNo"
            @trim="validateField('bankAccountNo')"
          />

          <BaseInput
            v-model="form.bankName"
            label="Bank name"
            placeholder="Kasikornbank"
            required
            cypress="bank-name"
            :error="errors.bankName"
            @trim="validateField('bankName')"
          />
        </div>

        <BaseInput
          v-model="form.nationalIdNumber"
          label="National ID number"
          placeholder="1-2345-67890-12-3"
          required
          cypress="card-no"
          :error="errors.nationalIdNumber"
          @trim="validateField('nationalIdNumber')"
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div>
            <label class="block mb-1 text-sm text-gray-300"
              >National ID (front) *</label
            >
            <input
              type="file"
              accept="image/*"
              @change="(e) => onFileChange(e, 'nationalIdFront')"
              class="card-photo-front w-full text-sm file:mr-4 file:rounded file:border-0 file:bg-white file:text-black file:px-3 file:py-2 file:cursor-pointer"
            />
            <p v-if="errors.nationalIdFront" class="text-red-500 text-sm mt-1">
              {{ errors.nationalIdFront }}
            </p>
          </div>
          <div>
            <label class="block mb-1 text-sm text-gray-300"
              >National ID (back) *</label
            >
            <input
              type="file"
              accept="image/*"
              @change="(e) => onFileChange(e, 'nationalIdBack')"
              class="card-photo-back w-full text-sm file:mr-4 file:rounded file:border-0 file:bg-white file:text-black file:px-3 file:py-2 file:cursor-pointer"
            />
            <p v-if="errors.nationalIdBack" class="text-red-500 text-sm mt-1">
              {{ errors.nationalIdBack }}
            </p>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex gap-3 mt-10">
        <button
          type="button"
          class="submit-button flex-1 font-bold text-md bg-white text-black px-4 py-3 rounded-full shadow-md hover:bg-gradient-to-r hover:from-blue-500 hover:to-purple-500 hover:text-white hover:shadow-xl hover:scale-105 transition-all duration-300 ease-out disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="submitDisabled"
          @click="onSubmit"
        >
          {{ auth.isSubmitting ? "Submitting.." : "Submit" }}
        </button>
        <button
          type="button"
          class="cancel-button flex-1 bg-neutral-800 text-white px-4 py-3 rounded-full hover:bg-neutral-700 transition"
          @click="router.back()"
        >
          Cancel
        </button>
      </div>
      <p class="text-center text-gray-500 text-sm mt-4">
        By registering, you agree to our terms & conditions.
      </p>
    </div>
  </div>
</template>
