export default{
    install: (app) => {
       app.config.globalProperties.$formatToThaiTime = (dateString) => {
        const date = new Date(dateString)
        return date.toLocaleString('th-TH',{
            timeZone:'Asia/Bangkok',
            hour12: false,
            year:'numeric',
            month:'2-digit',
            day:'2-digit',
            minute:'2-digit',
            second:'2-digit'
        })
       } 
    }
}