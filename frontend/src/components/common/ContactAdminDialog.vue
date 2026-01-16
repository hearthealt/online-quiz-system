<template>
  <ContactAdminView v-model="showDialog" />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import ContactAdminView from '@/views/ContactAdminView.vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const showDialog = ref(props.modelValue)

watch(() => props.modelValue, (newValue) => {
  showDialog.value = newValue
})

watch(showDialog, (newValue) => {
  emit('update:modelValue', newValue)
})
</script>
