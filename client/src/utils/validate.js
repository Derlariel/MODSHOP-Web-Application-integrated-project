const validateIsNumber = data => !isNaN (Number (data));
const validateContainsNumber = data => /\d/.test (data);
const validateNoWhitespace = data => !/\s/.test (data);
const validateMinLength = (data, len = 8) => data.length >= len;
const validateMaxLength = (data, len = 20) => data.length <= len;
const validateExactLength = (data, len = 10) => data.length === len;

export const checkUptodate = (data) => {
    if (data === null || !data) {
    sessionStorage.setItem(
      "error-message",
      "The requested sale item does not exist."
    );
    return true;
  }
}