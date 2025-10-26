export const runValidation = (data, validators = []) => {
  for (const validate of validators) {
    const {valid, message} = validate (data);
    if (!valid) return {valid, message};
  }
  return {valid: true, message: null};
};

export const validateMinLength = len => data => {
  const valid = data.length >= len;
  return {
    valid,
    message: valid ? null : `Minimum length is ${len} characters`,
  };
};

export const validateMaxLength = len => data => {
  const valid = data.length <= len;
  return {
    valid,
    message: valid ? null : `Maximum length is ${len} characters`,
  };
};

export const validateExactLength = len => data => {
  const valid = data.length === len;
  return {
    valid,
    message: valid ? null : `Length must be exactly ${len} characters`,
  };
};

export const validateContainsNumber = data => {
  const valid = /\d/.test (data);
  return {valid, message: valid ? null : 'Must contain at least one number'};
};

export const validatePositiveInteger = data => {
  const num = Number (data);
  const valid =
    data === '' && !isNaN (num) && Number.isInteger (num) && num >= 0;
  return {
    valid,
    message: valid ? null : 'Must be a non-negative integer',
  };
};

export function validateEmailPassword({ email, password }) {
  const errors = { email: "", password: "" };
  let valid = true;

  if (!email) {
    errors.email = "Email is required.";
    valid = false;
  } else if (email.length > 50) {
    errors.email = "Email must not exceed 50 characters.";
    valid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errors.email = "Invalid email format.";
    valid = false;
  }

  if (!password) {
    errors.password = "Password is required.";
    valid = false;
  } else if (password.length > 14) {
    errors.password = "Password must not exceed 14 characters.";
    valid = false;
  }

  return { valid, errors };
}


export const validateNoWhitespace = data => {
  const valid = !/\s/.test (data);
  return {valid, message: valid ? null : 'Must not contain whitespace'};
};

// Field validators spacific for jarn sanit
export const validateModel = data => {
  const valid =
  data.length >= 1 && data.length <= 60 && /^[a-zA-Z0-9-\s]+$/.test (data);
  return {
    valid,
    message: valid ? null : 'Model must be 1-60 characters long.',
  };
};

export const validateBrandSelected = data => {
  const valid = data != null;
  return {
    valid,
    message: valid ? null : 'Brand must be selected.',
  };
};

export const validateDescription = data => {
  const valid = data.length >= 1 && data.length <= 65535;
  return {
    valid,
    message: valid ? null : 'Description must be 1-65,535 characters long.',
  };
};

export const validatePrice = data => {
  const num = Number (data);
  const valid = data !== null && !isNaN (num) && Number.isInteger (num) && num >= 0;
  return {
    valid,
    message: valid ? null : 'Price must be non-negative integer.',
  };
};

export const validateQuantity = data => {
  const num = Number (data);
  const valid = data !== null && !isNaN (num) && Number.isInteger (num) && num >= 0;
  return {
    valid,
    message: valid ? null : 'Quantity must be non-negative integer.',
  };
};

export const validateBrandName = data => {
  const valid = data.length >= 1 && data.length <= 30;
  return {
    valid,
    message: valid ? null : 'Brand name must be 1-30 characters long.',
  };
};

export const validateBrandURL = data => {
  const valid = data === '' || /^https?:\/\/[^\s$.?#].[^\s]*$/.test (data);
  return {
    valid,
    message: valid ? null : 'Brand URL must be a valid URL or not specified.',
  };
};

export const validateBrandOrigin = data => {
  const valid =
    data === '' ||
    data.length >= 1 && data.length <= 80;
  return {
    valid,
    message: valid
      ? null
      : 'Brand country of origin must be 1-80 characters long or not specified.',
  };
};

export const validateRamSize = data => {
  const valid = data == null || data > 0;
  return {
    valid,
    message: valid
      ? null
      : 'RAM size must be positive integer or not specified.',
  };
};


export const validateScreenSize = (data) => {
    if (data === null || typeof data === undefined || data === '') {
    return {
      valid: true,
      message: null,
    };
  }

  // const trimmed = data.trim();
  const StrData = String(data).trim();
  const num = Number(StrData);
  if (isNaN(num) || num <= 0) {
    return {
      valid: false,
      message: 'Screen size must be positive number with at most 2 decimal points or not specified.',
    };
  }

  // const decimalPlaces = (trimmed.split('.')[1] || '').length;
  const decimalPlaces = (StrData.split('.')[1] || '').length;
  // console.log("dec", decimalPlaces);
  if (decimalPlaces > 2) {
    
    return {
      valid: false,
      message: 'Screen size must be positive number with at most 2 decimal points or not specified.',
    };
  }

  return {
    valid: true,
    message: null
  };
};

export const validateStorageSize = data => {
  const valid = data == null || data > 0;
  return {
    valid,
    message: valid
      ? null
      : 'Storage size must be positive integer or not specified.',
  };
};

export const validateColor = data => {
  const valid = data === '' || (data.length >= 1 && data.length <= 40);
  return {
    valid,
    message: valid
      ? null
      : 'Color must be 1-40 characters long or not specified.',
  };
};



// Generic Not found validator
export const checkUpToDate = (data) => {
    if (data === null || !data) {
    sessionStorage.setItem(
      "error-message",
      "The requested sale item does not exist."
    );
    return true;
  }
}