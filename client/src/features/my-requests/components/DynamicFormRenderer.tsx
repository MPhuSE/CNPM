import React from 'react';
import { FormSchema, FormFieldSchema } from '@/shared/types/workflow';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { formatVND } from '@/shared/utils/formatters';

interface DynamicFormRendererProps {
  schema: FormSchema;
  values: Record<string, any>;
  onChange?: (key: string, value: any) => void;
  readOnly?: boolean;
  errors?: Record<string, string>;
}

export const DynamicFormRenderer: React.FC<DynamicFormRendererProps> = ({
  schema,
  values,
  onChange,
  readOnly = false,
  errors = {},
}) => {
  if (!schema?.fields || schema.fields.length === 0) {
    return <p className="text-sm text-gray-500 italic">Quy trình không có biểu mẫu nhập liệu.</p>;
  }

  const renderField = (field: FormFieldSchema) => {
    const val = values[field.key] ?? '';
    const error = errors[field.key];

    if (readOnly) {
      let displayVal = val;
      if (field.type === 'vnd') displayVal = formatVND(val);
      if (field.type === 'url' && val) {
        displayVal = (
          <a
            href={val}
            target="_blank"
            rel="noopener noreferrer"
            className="text-indigo-600 underline hover:text-indigo-800 break-all"
          >
            {val}
          </a>
        );
      }
      return (
        <div key={field.key} className="py-2 border-b border-gray-100 last:border-b-0">
          <span className="text-xs font-semibold text-gray-500 uppercase">{field.label}</span>
          <div className="mt-1 text-sm font-medium text-gray-900">{displayVal || '—'}</div>
        </div>
      );
    }

    switch (field.type) {
      case 'select':
        return (
          <Select
            key={field.key}
            label={field.label}
            required={field.required}
            value={val}
            error={error}
            options={[
              { label: '-- Chọn giá trị --', value: '' },
              ...(field.options || []),
            ]}
            onChange={(e) => onChange?.(field.key, e.target.value)}
          />
        );
      case 'number':
        return (
          <Input
            key={field.key}
            type="number"
            label={field.label}
            required={field.required}
            value={val}
            error={error}
            min={field.min}
            max={field.max}
            onChange={(e) => onChange?.(field.key, e.target.value)}
          />
        );
      case 'vnd':
        return (
          <div key={field.key}>
            <Input
              type="text"
              label={`${field.label} (VNĐ)`}
              required={field.required}
              value={val}
              error={error}
              placeholder="VD: 10000000"
              helperText={val ? `Quy đổi: ${formatVND(val)}` : 'Nhập số tiền nguyên (không dấu chấm/phẩy)'}
              onChange={(e) => {
                const numericOnly = e.target.value.replace(/\D/g, '');
                onChange?.(field.key, numericOnly);
              }}
            />
          </div>
        );
      case 'date':
        return (
          <Input
            key={field.key}
            type="date"
            label={field.label}
            required={field.required}
            value={val}
            error={error}
            onChange={(e) => onChange?.(field.key, e.target.value)}
          />
        );
      case 'url':
        return (
          <Input
            key={field.key}
            type="url"
            label={field.label}
            required={field.required}
            value={val}
            error={error}
            placeholder="https://..."
            helperText="Đường dẫn chứng từ ngoài (chỉ HTTPS)"
            onChange={(e) => onChange?.(field.key, e.target.value)}
          />
        );
      case 'text':
      default:
        return (
          <Input
            key={field.key}
            label={field.label}
            required={field.required}
            value={val}
            error={error}
            maxLength={field.maxLength}
            onChange={(e) => onChange?.(field.key, e.target.value)}
          />
        );
    }
  };

  return <div className="space-y-4">{schema.fields.map(renderField)}</div>;
};
