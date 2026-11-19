// Thin wrapper around the REST API. Every write returns the refreshed resident overview.

async function handle(response) {
  if (!response.ok) {
    let message = response.statusText || `Request failed (${response.status})`
    try {
      const body = await response.json()
      if (body.message) message = body.message
    } catch {
      // body was not JSON, keep the status text
    }
    throw new Error(message)
  }
  return response.json()
}

const get = (url) => fetch(url).then(handle)

const post = (url, body) =>
  fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }).then(handle)

export const getResidents = () => get('/api/residents')
export const getActivity = (hours) => get(`/api/activity?hours=${hours}`)

export const changeBed = (residentId, nurse) =>
  post(`/api/residents/${residentId}/bed-change`, { nurse })

export const recordMood = (residentId, body) => post(`/api/residents/${residentId}/mood`, body)

export const addMedication = (residentId, body) =>
  post(`/api/residents/${residentId}/medications`, body)

export const administer = (medicationId, body) =>
  post(`/api/medications/${medicationId}/administer`, body)

export const addTask = (residentId, body) => post(`/api/residents/${residentId}/tasks`, body)

export const completeTask = (taskId, nurse) => post(`/api/tasks/${taskId}/complete`, { nurse })

export const addNote = (residentId, body) => post(`/api/residents/${residentId}/notes`, body)

export const acknowledgeNote = (noteId, nurse) =>
  post(`/api/notes/${noteId}/acknowledge`, { nurse })
