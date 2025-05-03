/**
 * @param {import('vue-router').Router} router
 * @param {String} url
 * @param {RequestInit} options
 * 
 */

import router from '@/router';

export async function fetchWithErrorHandling(router,url, options ={}) {
  try {
    const res = await fetch(url, options);

    if(!res.ok){
      const code = res.status || 500
      router.push({name:'error-page',query:{code}})
      return null
    }

    const data = await res.json()
    return data
  }catch(err){
    router.push({name:'error-page',query:{code:500}})
  }
}